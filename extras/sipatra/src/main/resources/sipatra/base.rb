require 'sipatra/helpers'
require 'sipatra/extension_modules'
require 'benchmark'

module Sipatra
  VERSION = '1.0.0'
  
  class Base
    include HelperMethods
    attr_accessor :sip_factory, :context, :session, :message, :params, :log
    alias :request  :message 
    alias :response :message 
            
    def initialize()
      @params = Hash.new { |hash,key| hash[key.to_s] if Symbol === key }
    end
    
    # called from Java to set SIP servlet bindings
    def set_bindings(*args)
      @context, @sip_factory, @session, @message, @log = args
      session.extend Sipatra::SessionExtension
      message.extend Sipatra::MessageExtension
    end

    # called to process a SIP request
    def do_request
      call! self.class.req_handlers
    end
    
    # called to process a SIP response
    def do_response
      call! self.class.resp_handlers
    end

    # Access settings defined with Base.set.
    def self.settings
      self
    end

    # Access settings defined with Base.set.
    def settings
      self.class.settings
    end

    # Exit the current block, halts any further processing
    # of the message.
    # TODO: handle a response (as param)
    def halt
      throw :halt
    end
    
    # Pass control to the next matching handler.
    def pass
      throw :pass
    end
    
    def request?
      !message.respond_to?(:getRequest)
    end
    
    def response?
      message.respond_to?(:getRequest)
    end
    
    private
    
    def session=(session)
      @session = session
      class << @session
         include SessionExtension
       end
    end

    # Retrieve the OUTBOUND_INTERFACES value from cipango.
    def get_addresses
      context.getAttribute('javax.servlet.sip.outboundInterfaces')
    end
    
    def message_type
      response? ? :response : :request
    end
    
    def eval_options(opts)
      opts.each_pair { |key, condition|
        pass unless header? key
        header_match = condition.match header[key]
        @params[key] = header_match.to_a if header_match
      }
    end
    
    def eval_condition(arg, keys, opts)
      #clear (for multi usage)
      @params.clear
      if request?
        match = arg.match message.requestURI.to_s
        if match
          eval_options(opts)
          if keys.any?
            values = match.captures.to_a #Array of matched values
            keys.zip(values).each do |(k, v)|
              @params[k] = v
            end
          elsif(match.length > 1)
            @params[:uri] = match.to_a
          end
          return true
        end
      else
        if ((arg == 0) or (arg == message.status))
          eval_options(opts)
          return true
        end
      end
      return false
    end
    
    def process_handler(handlers_hash, method_or_joker)
      if handlers = handlers_hash[method_or_joker]
        handlers.each do |pattern, keys, opts, block|
          catch :pass do
            throw :pass unless eval_condition(pattern, keys, opts)
            throw :halt, instance_eval(&block)          
          end
        end
      end
    end
    
    # Run all filters defined on superclasses and then those on the current class.
    def filter!(type, base = self.class)
      filter! type, base.superclass if base.superclass.respond_to?(:filters)
      base.filters[type].each { |block| instance_eval(&block) }
    end
    
    def call!(handlers)
      filter! :before
      catch(:halt) do
        process_handler(handlers, message.method)
        process_handler(handlers, "_")
      end
    ensure 
      filter! :after
    end
    
    class << self
      attr_reader :req_handlers, :resp_handlers, :filters
      
      # permits configuration of the application
      def configure(*envs, &block)
        yield self if envs.empty? || envs.include?(environment.to_sym)
      end

      # Sets an option to the given value.  If the value is a proc,
      # the proc will be called every time the option is accessed.
      def set(option, value=self, &block)
        raise ArgumentError if block && value != self
        value = block if block
        if value.kind_of?(Proc)
          metadef(option, &value)
          metadef("#{option}?") { !!__send__(option) }
          metadef("#{option}=") { |val| metadef(option, &Proc.new{val}) }
        elsif value == self && option.respond_to?(:each)
          option.each { |k,v| set(k, v) }
        elsif respond_to?("#{option}=")
          __send__ "#{option}=", value
        else
          set option, Proc.new{value}
        end
        self
      end

      # Same as calling `set :option, true` for each of the given options.
      def enable(*opts)
        opts.each { |key| set(key, true) }
      end

      # Same as calling `set :option, false` for each of the given options.
      def disable(*opts)
        opts.each { |key| set(key, false) }
      end

      # Methods defined in the block and/or in the module
      # arguments available to handlers.
      def helpers(*modules, &block)
        include(*modules) if modules.any?
        class_eval(&block) if block_given?
      end
      
      # Extension modules registered on this class and all superclasses.
      def extensions
        if superclass.respond_to?(:extensions)
          (@extensions + superclass.extensions).uniq
        else
          @extensions
        end
      end
      
      # Extends current class with all modules passed as arguements
      # if a block is present, creates a module with the block and
      # extends the current class with it.
      def register_extension(*extensions, &block)
        extensions << Module.new(&block) if block_given?
        @extensions += extensions
        extensions.each do |extension|
          extend extension
          extension.registered(self) if extension.respond_to?(:registered)
        end
      end      
      
      def response(*args, &block)
        method_name = args.shift if (!args.first.kind_of? Hash) and (!args.first.kind_of? Integer)
        code_int = args.shift if !args.first.kind_of? Hash
        opts = *args
        pattern = code_int || 0
        sip_method_name = method_name ? method_name.to_s.upcase : "_"
        handler("response_#{sip_method_name}  \"#{pattern}\"", sip_method_name, pattern, [], opts || {}, &block)
      end
      
      [:ack, :bye, :cancel, :info, :invite, :message, 
       :notify, :options, :prack, :publish, :refer, 
       :register, :subscribe, :update, :request].each do |name|
        define_method name do |*args, &block|
          path = args.shift if (!args.first.kind_of? Hash)
          opts = *args
          uri = path || //
          pattern, keys = compile_uri_pattern(uri)
          sip_method_name = name == :request ? "_" : name.to_s.upcase
          handler("request_#{sip_method_name}  \"#{uri.kind_of?(Regexp) ? uri.source : uri}\"", sip_method_name, pattern, keys , opts || {}, &block)
        end
      end
      
      def before(message_type = nil, &block)
        add_filter(:before, message_type, &block)
      end
      
      def after(message_type = nil, &block)
        add_filter(:after, message_type, &block)
      end
            
      def reset!
        @req_handlers          = {}
        @resp_handlers         = {}
        @extensions            = []
        @filters               = {:before => [], :after => []}
      end

      def inherited(subclass)
        subclass.reset!
        super
      end
      
      def before_filters
        filters[:before]
      end

      def after_filters
        filters[:after]
      end
      
      def invoke_hook(name, *args)
        extensions.each { |e| e.send(name, *args) if e.respond_to?(name) }
      end

      private
      
      def add_filter(type, msg_type = nil, &block)
        if msg_type
          add_filter(type) do
            next unless msg_type == message_type
            instance_eval(&block)
          end
        else
          filters[type] << block
        end
      end

      def metadef(message, &block)
        (class << self; self; end).
          send :define_method, message, &block
        if !['?', '='].include?(message.to_s[-1, 1])
          invoke_hook(:option_set, self, message)
        end
      end

      # compiles a URI pattern
      def compile_uri_pattern(uri)
        keys = []
        if uri.respond_to? :to_str
          pattern =
          uri.to_str.gsub(/\(:(\w+)\)/) do |match|
            keys << $1.dup
                "([^:@;=?&]+)"
          end
          [/^#{pattern}$/, keys]
        elsif uri.respond_to? :match
          [uri, keys]
        else
          raise TypeError, uri
        end
      end
      
      def handler(method_name, verb, pattern, keys, options={}, &block)
        raise ArgumentError, "A block should be given to a handler definition method" if block.nil?
        define_method method_name, &block
        unbound_method = instance_method(method_name)
        block =
        if block.arity != 0
          proc { unbound_method.bind(self).call(*@block_params) }
        else
          proc { unbound_method.bind(self).call }
        end
        handler_table(method_name, verb).push([pattern, keys, options, block]).last # TODO: conditions  
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
    def self.register_extension(*extensions, &block) #:nodoc:
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
    
    delegate :ack, :bye, :cancel, :info, :invite, :message,
      :notify, :options, :prack, :publish, :refer, 
      :register, :subscribe, :update, 
      :helpers, :configure, :settings, :set, :enable, :disable,
      :before, :after, :request, :response
  end
  
  def self.helpers(*extensions, &block)
    Application.helpers(*extensions, &block)
  end  
  
  def self.register_extension(*extensions, &block)
    Application.register_extension(*extensions, &block)
  end
end
