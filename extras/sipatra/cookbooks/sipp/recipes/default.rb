#
# Cookbook Name:: sipp 
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

include_recipe "build-essential"

package "libncurses5-dev"

bash "install_sipp" do
  user "root"
  cwd "/tmp"
  code <<-EOH
  (cd /tmp; wget http://heanet.dl.sourceforge.net/project/sipp/sipp/3.2/sipp.svn.tar.gz) 
  (cd /tmp; tar zxvf sipp.svn.tar.gz)
  (cd /tmp/sipp.svn; make && cp -a sipp /usr/local/bin/)
  EOH
  not_if { ::File.exists?("/usr/local/bin/sipp") }
end
