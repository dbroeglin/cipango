#
# Cookbook Name:: asterisk 
# Recipe:: default
#
# Copyright 2011, Dominique Broeglin
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include_recipe "apt"

package "asterisk"

service "asterisk" do
  supports :start=> true, :stop => true, :restart => true, :status => true
  action :enable
end

%w{ asterisk.conf extensions.conf features.conf indications.conf
    logger.conf manager.conf modules.conf rtp.conf sip.conf }.each do |filename|  
  cookbook_file "/etc/asterisk/#{filename}" do 
    source filename 
    owner "asterisk" 
    group "asterisk" 
    mode 0640 
    notifies :restart, resources(:service => "asterisk"), :delayed
  end 
end
