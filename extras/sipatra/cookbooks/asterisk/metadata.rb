maintainer       "Dominique Broeglin"
maintainer_email "dominique.broeglin@gmail.com"
license          "Apache 2.0"
description      "Installs Asterisk"
long_description "Installs Asterisk" 
version          "1.0.0"

recipe "asterisk", "Installs asterisk"

%w{ apt }.each do |cb|
  depends cb
end

%w{ ubuntu debian centos rhel arch }.each do |os|
  supports os
end
