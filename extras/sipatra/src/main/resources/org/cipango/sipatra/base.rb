require 'java'
require 'org/cipango/sipatra/helpers'

module Sipatra
  VERSION = '1.0.0'
  
  java_import javax.servlet.sip.SipServletResponse
  
  class Base
    include HelperMethods
    attr_accessor :sip_factory, :context, :session, :message
    
    def set_bindings(*args)
      @context, @sip_factory, @session, @message = args
    end
    
    def do_request
      puts "DO REQUEST: #{message.method} #{message.requestURI}"
      processed = process_handler(self.class.req_handlers, message.method)
      if !processed
        process_handler(self.class.req_handlers, "REQUEST")
      end
    end
    
    def do_response
      puts "DO RESPONSE: #{message.status} #{message.method}"
      processed = process_handler(self.class.resp_handlers, message.method)
      if !processed
        process_handler(self.class.resp_handlers, "ALL")
      end
    end
    
    def eval_arg?(arg)
      #TODO: ugly
      if message.respond_to? :requestURI
        return arg.match message.requestURI.to_s
      else
        return ((arg == 0) or (arg == message.status))
      end
    end
    
    def process_handler(tab_handler, value)
      processed = false
      if handlers = tab_handler[value]
        handlers.each { |pattern, keys, conditions, block|
          if eval_arg?(pattern)
            # TODO: use keys and conditions
            processed = true
            instance_eval(&block)          
            break
          end
        }
      end
      return processed
    end
    
    class << self
      attr_reader :req_handlers
      attr_reader :resp_handlers
      
      # permits configuration of the application
      def configure(*envs, &block)
        yield self if envs.empty? || envs.include?(environment.to_sym)
      end
      
      private
      
      def reset!
        @req_handlers         = {}
        @resp_handlers         = {}
      end
      
      # compiles a URI pattern
      def compile_uri_pattern(uri)
        puts "Compile: #{uri}"
        keys = [] # TODO: Not yet used, shall contain key names
        if uri.respond_to? :to_str
          [/^#{uri}$/, keys]
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
        #TODO: ugly
        if(method_name.include? "RESPONSE_")
         ((@resp_handlers ||= {})[verb] ||= []).
          push([pattern, keys, nil, block]).last # TODO: conditions  
        else
         ((@req_handlers ||= {})[verb] ||= []).
          push([pattern, keys, nil, block]).last # TODO: conditions  
        end      
      end         
      
      [:ack, :bye, :cancel, :info, :invite, :message, 
       :notify, :options, :prack, :publish, :refer, 
       :register, :subscribe, :update, :request].each do |name|
        define_method name do |*args, &block|
          path, opts = *args
          uri = path || //
          pattern, keys = compile_uri_pattern(uri)
          handler("#{name.to_s.upcase}  \"#{uri.kind_of?(Regexp) ? uri.source : uri}\"", name.to_s.upcase, pattern, keys , opts || {}, &block)
        end
      end
      
      def response(*args, &block)
        method_name, code_int, opts = *args
        pattern = code_int || 0
        handler("RESPONSE_#{method_name.to_s.upcase || "ALL"}  \"#{pattern}\"", method_name.to_s.upcase || "ALL", pattern, [], opts || {}, &block)
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
    
    delegate :invite, :register, :request, :response
  end
end

include Sipatra::Delegator
