require 'java'
require 'org/cipango/sipatra/helpers'

module Sipatra
  VERSION = '1.0.0'
  
  java_import javax.servlet.sip.SipServletResponse
  
  class Base
    include HelperMethods
    attr_accessor :sip_factory, :context, :session, :msg, :params
    
    def initialize()
      @params = Hash.new {|hash,key| hash[key.to_s] if Symbol === key }
    end
    
    # called from Java to set SIP servlet bindings
    def set_bindings(*args)
      @context, @sip_factory, @session, @msg = args
    end
    
    # called to process a SIP request
    def do_request
      call self.class.req_handlers
    end
    
    # called to process a SIP response
    def do_response
      call self.class.resp_handlers
    end
    
    # Exit the current block, halts any further processing
    # of the message.
    def halt
      throw :halt
    end
    
    # Pass control to the next matching handler.
    def pass
      throw :pass
    end
    
    private
    
    def eval_condition(arg, keys)
      #TODO: ugly
      if msg.respond_to? :requestURI
        match = arg.match msg.requestURI.to_s
        if match
          params=
          if keys.any?
            values = match.captures.to_a #Array of matched values
            keys.zip(values).inject({}) do |hash,(k,v)| #keys.zip(values) build an Array containaing Arrays of containing 2 elements "key and value"
              hash[k] = v
              hash
            end
          elsif(match.length > 1)
            {:uri => match.to_a}
          else
            {}
          end
          @params.merge!(params)
        end
        return match
      else
        return ((arg == 0) or (arg == msg.status))
      end
    end
    
    def process_handler(handlers_hash, method_or_joker)
      if handlers = handlers_hash[method_or_joker]
        handlers.each do |pattern, keys, conditions, block|
          catch :pass do
            throw :pass unless eval_condition(pattern, keys)
            throw :halt, instance_eval(&block)          
          end
        end
      end
    end
    
    def call(handlers)
      catch(:halt) do
        process_handler(handlers, msg.method)
        process_handler(handlers, "_")
      end
    end

    class << self
      attr_reader :req_handlers
      attr_reader :resp_handlers
      
      # permits configuration of the application
      def configure(*envs, &block)
        yield self if envs.empty? || envs.include?(environment.to_sym)
      end
      
      # Methods defined in the block and/or in the module
      # arguments available to handlers.
      def helpers(*modules, &block)
        include(*modules) if modules.any?
        class_eval(&block) if block_given?
      end
      
      def response(*args, &block)
        method_name, code_int, opts = *args
        pattern = code_int || 0
        sip_method_name = method_name ? method_name.to_s.upcase : "_"
        handler("response_#{sip_method_name}  \"#{pattern}\"", sip_method_name, pattern, [], opts || {}, &block)
      end
      
      [:ack, :bye, :cancel, :info, :invite, :message, 
       :notify, :options, :prack, :publish, :refer, 
       :register, :subscribe, :update, :request].each do |name|
        define_method name do |*args, &block|
          path, opts = *args
          uri = path || //
          pattern, keys = compile_uri_pattern(uri)
          sip_method_name = name == :request ? "_" : name.to_s.upcase
          handler("request_#{sip_method_name}  \"#{uri.kind_of?(Regexp) ? uri.source : uri}\"", sip_method_name, pattern, keys , opts || {}, &block)
        end
      end
      
      private
      
      def reset!
        @req_handlers          = {}
        @resp_handlers         = {}
      end
      
      # compiles a URI pattern
      def compile_uri_pattern(uri)
        keys = []
        if uri.respond_to? :to_str
          pattern =
          uri.to_str.gsub(/\(:(\w+)\)/) do |match|
            keys << $1.dup
                "(.*)"
          end
          [/^#{pattern}$/, keys]
        elsif uri.respond_to? :match
          [uri, keys]
        else
          raise TypeError, uri
        end
      end
      
      def handler(method_name, verb, pattern, keys, options={}, &block)
        define_method method_name, &block
        unbound_method = instance_method(method_name)
        block =
        if block.arity != 0
          proc { unbound_method.bind(self).call(*@block_params) }
        else
          proc { unbound_method.bind(self).call }
        end
        handler_table(method_name, verb).push([pattern, keys, nil, block]).last # TODO: conditions  
      end         
      
      def handler_table(method_name, verb)
        if method_name.start_with? "response"
         (@resp_handlers ||= {})[verb] ||= []
        else
         (@req_handlers ||= {})[verb] ||= []
        end
      end   
    end
    
    reset!
  end
  
  class Application < Base    
    def self.register(*extensions, &block) #:nodoc:
      added_methods = extensions.map {|m| m.public_instance_methods }.flatten
      Delegator.delegate(*added_methods)
      super(*extensions, &block)
    end    
  end
  
  module Delegator #:nodoc:
    def self.delegate(*methods)
      methods.each do |method_name|
        eval <<-RUBY, binding, '(__DELEGATE__)', 1
          def #{method_name}(*args, &b)
            ::Sipatra::Application.send(#{method_name.inspect}, *args, &b)
          end
          private #{method_name.inspect}
        RUBY
      end
    end
    
    delegate :request, :response, :helpers,
      :ack, :bye, :cancel, :info, :invite, 
      :notify, :options, :prack, :publish, :refer, 
      :register, :subscribe, :update
  end
  
  def self.helpers(*extensions, &block)
    Application.helpers(*extensions, &block)
  end  
end

include Sipatra::Delegator
