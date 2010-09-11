if  ENV['SIPATRA_PATH']
  ENV['GEM_HOME'] = ENV['SIPATRA_PATH'] + '/../gems'
  ENV['GEM_PATH'] = ENV['SIPATRA_PATH'] + '/../gems'
  require 'rubygems'
end

require 'sipatra/base'

include Sipatra::Delegator
