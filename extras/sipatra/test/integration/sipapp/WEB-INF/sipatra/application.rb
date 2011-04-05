invite /.*/ do
  puts "#{message.method} #{message.requestURI}"
  proxy "sip:#{message.requestURI.user}@127.0.100.1:5060"
end

register do
  puts "#{message.method} #{message.requestURI}"
  proxy "sip:#{message.requestURI.user}@127.0.100.1:5060"
end

request do
  puts "#{message.method} #{message.requestURI} [default]"
  proxy "sip:#{message.requestURI.user}@127.0.100.1:5060"
end

#ack do
#  puts "#{message.method} #{message.requestURI}"
#  proxy "sip:toto@127.0.2.1:5062"
#end

response do
 puts "RESPONSE: #{message.status} #{message.request.requestURI}"
end
