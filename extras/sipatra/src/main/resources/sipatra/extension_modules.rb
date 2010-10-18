module Sipatra
  module MessageExtension
    def uri
      requestURI
    end
  end
  
  module SessionExtension
    def [](name)
      getAttribute(name.to_s)
    end
    
    def []=(name, value)
      if value.nil?
        removeAttribute(name.to_s)
      else
        setAttribute(name.to_s, value)
      end
    end
  end
end