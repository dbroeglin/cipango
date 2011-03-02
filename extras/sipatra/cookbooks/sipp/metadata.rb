maintainer       "Dominique Broeglin"
maintainer_email "dominique.broeglin@gmail.com"
license          "Apache 2.0"
description      "Installs SIPp"
long_description "Installs SIPp a free Open Source test tool / traffic generator for the SIP protocol" 
version          "1.0.0"

recipe "sipp", "Installs sipp"

%w{ build-essential }.each do |cb|
  depends cb
end

%w{ ubuntu debian centos rhel arch }.each do |os|
  supports os
end
