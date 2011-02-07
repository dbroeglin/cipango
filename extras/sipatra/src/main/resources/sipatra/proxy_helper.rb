require 'java'

module Sipatra
  
  module ProxyHelper
    def proxy(*args)
      uri = args.shift
      uri, options = nil, uri if uri.kind_of? Hash
      options ||= args.shift || {}
      if uri.nil?
        uri = message.requestURI
      elsif uri.kind_of? Array
        uris_as_strings = uri
        uri = java.util.ArrayList.new
        uris_as_strings.each do |uri_as_string|
          uri.add(sip_factory.createURI(uri_as_string))
        end
      else
        uri = sip_factory.createURI(uri)
      end
      proxy = message.proxy
      proxy.setRecordRoute(options[:record_route]) if options.has_key? :record_route
      proxy.setParallel(options[:parallel]) if options.has_key? :parallel
      proxy.setProxyTimeout(options[:timeout]) if options.has_key? :timeout
      proxy.setAddToPath(options[:add_to_path]) if options.has_key? :add_to_path
      proxy.setRecurse(options[:recurse]) if options.has_key? :recurse
      proxy.setSupervised(options[:supervised]) if options.has_key? :supervised
      proxy.proxyTo(uri)
    end  
  end
end
