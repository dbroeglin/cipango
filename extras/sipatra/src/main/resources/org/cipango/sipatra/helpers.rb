module Sipatra
  class HeadersWrapper
    def initialize(app, plural = false, address = false)
      @app = app
      method_definitions = <<-RUBY
        def [](name)
          @app.message.get#{address ? "Address" : ""}Header#{plural ? "s" : ""}(name.to_s)
        end
      RUBY
      if plural
        method_definitions += <<-RUBY
          def []=(name, values)
            name = name.to_s
            @app.message.remove#{address ? "Address" : ""}Header(name)
            values.each { |value| @app.message.add#{address ? "Address" : ""}Header(name, value.to_s) }
          end 
        RUBY
      else
        method_definitions += <<-RUBY
          def []=(name, value)
            @app.message.set#{address ? "Address" : ""}Header(name.to_s, value)
          end 
        RUBY
      end
      class << self; self; end.class_eval method_definitions
      end 
    end
    
    module HelperMethods  
      def proxy(*args)
        uri = args.shift
        uri, options = nil, uri if uri.kind_of? Hash
        options ||= args.shift || {}
        if uri.nil?
          uri = message.requestURI
        else
          uri = sip_factory.createURI(uri)
        end
        proxy = message.getProxy()
        proxy.setRecordRoute(options[:record_route]) if options.has_key? :record_route
        proxy.proxyTo(uri)
      end    
      
      def header
        @header_wrapper ||= HeadersWrapper::new(self)
      end
      
      def headers
        @headers_wrapper ||= HeadersWrapper::new(self, true)
      end
      
      def address_header
        @address_header_wrapper ||= HeadersWrapper::new(self, false, true)
      end
      
      def address_headers
        @address_headers_wrapper ||= HeadersWrapper::new(self, true, true)
      end
      
      def add_header(name, value)
        message.addHeader(name.to_s, value)
      end
      
      def add_address_header(name, value)
        message.addAddressHeader(name.to_s, value)
      end
      
      def header?(name)
        !message.getHeader(name.to_s).nil?
      end
      
      def modify_header(header_name, pattern = nil, new_value = nil)
        #FIXME: "JAVA" Code
        if pattern
          pattern = Regexp.new(/^#{pattern}$/) unless pattern.kind_of? Regexp
          headers[header_name] = headers[header_name].map do |value|
            value.gsub(pattern, new_value)
          end
        else
          headers[header_name] = headers[header_name].map do |value|
            yield value
          end
        end
      end
      
      def remove_header(name)
        message.removeHeader(name.to_s)
      end
      
      def send_response(status, *args)
        create_args = [convert_status_code(status)]
        create_args << args.shift unless args.empty? || args.first.kind_of?(Hash)
        response = message.createResponse(*create_args)
        unless args.empty?
          raise ArgumentError, "last argument should be a Hash" unless args.first.kind_of? Hash
          args.first.each_pair do |name, value|
            response.addHeader(name.to_s, value.to_s)
          end
        end
        if block_given?
          yield response
        end
        response.send
      end
      
      def create_address(addr, options = {})
        addr = addr.to_s # TODO: Handle URI instances
        address = sip_factory.createAddress(addr)
        address.setExpires(options[:expires]) if options.has_key? :expires
        address.setDisplayName(options[:display_name]) if options.has_key? :display_name
        
        address      
      end
      
      def push_route(route)
        message.pushRoute(sip_factory.createAddress(route))
      end    
      
      private
      
      def convert_status_code(symbol_or_int)
        case symbol_or_int
          when Integer: return symbol_or_int
          when Symbol
          begin
            SipServletResponse.class_eval("SC_#{symbol_or_int.to_s.upcase}")
          rescue NameError => e
            raise ArgumentError, "Unknown status code symbol: '#{symbol_or_int}' (#{e.message})"
          end
        else
          raise ArgumentError, "Status code value should be a Symbol or Int not '#{symbol_or_int.class}'"
        end
      end
    end
  end