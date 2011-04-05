module Sipatra
  STATUS_CODES_MAP = {
    :accepted => 202,
    :address_incomplete => 484,
    :alternative_service => 380,
    :ambiguous => 485,
    :bad_event => 489,
    :bad_extension => 420,
    :bad_gateway => 502,
    :bad_identity_info => 436,
    :bad_request => 400,
    :busy_everywhere => 600,
    :busy_here => 486,
    :call_being_forwarded => 181,
    :call_leg_done => 481,
    :call_queued => 182,
    :conditional_request_failed => 412,
    :decline => 603,
    :does_not_exit_anywhere => 604,
    :extension_required => 421,
    :forbidden => 403,
    :gone => 410,
    :interval_too_brief => 423,
    :invalid_identity_header => 438,
    :loop_detected => 482,
    :message_too_large => 513,
    :method_not_allowed => 405,
    :moved_permanently => 301,
    :moved_temporarily => 302,
    :multiple_choices => 300,
    :not_acceptable => 406,
    :not_acceptable_anywhere => 606,
    :not_acceptable_here => 488, 
    :not_found => 404,
    :not_implemented => 501,
    :ok => 200,
    :payment_required => 402,
    :precondition_failure => 580,
    :provide_referer_identity => 429,
    :proxy_authentication_required => 407,
    :request_entity_too_large => 413,
    :request_pending => 491,
    :request_terminated => 487,
    :request_timeout => 408,
    :request_uri_too_long => 414,
    :ringing => 180,
    :security_agreement_required => 494,
    :server_internal_error => 500,
    :server_timeout => 504,
    :service_unavailable => 503,
    :session_interval_too_small => 422,
    :session_progress => 183,
    :temporarily_unavailable => 480,
    :too_many_hops => 483,
    :trying => 100,
    :unauthorized => 401,
    :undecipherable => 493,
    :unsupported_certificate => 437,
    :unsupported_media_type => 415,
    :unsupported_uri_scheme => 416,
    :use_identity_header => 428,
    :use_proxy => 305,
    :version_not_supported => 505,
  }
  
  class HeadersWrapper
    def initialize(app, plural = false, address = false)
      @app = app
      method_definitions = <<-RUBY
        def [](name)
          @app.message.get#{address ? "Address" : ""}Header#{plural ? "s" : ""}(name.to_s)#{plural ? ".to_a" : ""}
        end
      RUBY
      if plural
        method_definitions += <<-RUBY
          def []=(name, values)
            name = name.to_s
            @app.message.removeHeader(name)
            if !values.nil?
              values.each do |value| 
                @app.message.add#{address ? "Address" : ""}Header(name, value#{address ? ", true" : ".to_s"})
              end
            end
          end 
        RUBY
      else
        method_definitions += <<-RUBY
          def []=(name, value)
            if !value.nil?
              @app.message.set#{address ? "Address" : ""}Header(name.to_s, value)
            else
              @app.message.removeHeader(name.to_s)
            end
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
        proxy = message.proxy
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
      
      def add_address_header(name, value, first = true)
        message.addAddressHeader(name.to_s, value, first)
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
      
      def create_uri(value, options = {})
        uri = sip_factory.createURI(value)
        uri.setLrParam((options.has_key? :lr) ? options[:lr] : true)
        uri
      end

      def push_route(route)
        message.pushRoute(sip_factory.createAddress(route))
      end    
      
      private
      
      def convert_status_code(symbol_or_int)
        case symbol_or_int
          when Integer: return symbol_or_int
          when Symbol: 
          code = STATUS_CODES_MAP[symbol_or_int]
          raise ArgumentError, "Unknown status code symbol: '#{symbol_or_int}'" unless code
          code
        else
          raise ArgumentError, "Status code value should be a Symbol or Int not '#{symbol_or_int.class}'"
        end
      end
    end
  end
