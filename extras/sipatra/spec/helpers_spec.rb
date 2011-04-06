require File.dirname(__FILE__) + '/helper'

class FakeApp 
  include Sipatra::HelperMethods
  
  public :message # or we would NOT have access to the WRONG private method of Object
end

describe 'When', Sipatra::HelperMethods, 'is included; ', FakeApp do
  def mock_address
    @mock_address ||= mock('Address')
  end
  
  def mock_uri
    @mock_uri ||= mock('URI')
  end
  
  def mock_proxy
    @mock_proxy ||= mock('Proxy')
  end
  
  def mock_sip_factory
    @sip_factory ||= mock('SipFactory')
  end
  
  def mock_response
    @mock_response ||= mock('SipServletResponse')
  end
  
  subject do
    FakeApp::new
  end
  
  before do
    subject.stub!(:message).and_return(Object::new)
  end
  
  describe "#convert_status_code" do
    it "should convert Integer to an Integer" do
      subject.send(:convert_status_code, 400).should be_kind_of(Integer)
    end
    
    it "should convert a Symbol to it's numeric equivalent" do
      subject.send(:convert_status_code, :not_found).should == 404
    end
  end
  
  describe "#remove_header" do
    it "should remove the header with the given name" do
      subject.message.should_receive(:removeHeader).twice.with('toto')
      
      subject.remove_header(:toto)
      subject.remove_header('toto')
    end
  end
  
  describe "#modify_header" do
    # TODO: what if we have multiple headers ?
    it 'should replace a header value with substitution' do
      mock_headers = mock('HeadersWrapper')
      subject.should_receive(:headers).twice.and_return(mock_headers)
      mock_headers.should_receive(:"[]").with('X-Header').and_return(['old_value'])
      mock_headers.should_receive(:"[]=").with('X-Header', ['new_value'])
      
      subject.modify_header 'X-Header', /^old_(value)$/, 'new_\1'
    end
    
    it 'should replace multiple headers with substitution' do
      mock_headers = mock('HeadersWrapper')
      subject.should_receive(:headers).twice.and_return(mock_headers)
      mock_headers.should_receive(:"[]").with('X-Header').and_return(['old_value', 'old_foo'])
      mock_headers.should_receive(:"[]=").with('X-Header', ['new_value', 'new_foo'])
      
      subject.modify_header 'X-Header', /^old_(.*)$/, 'new_\1'
    end
    
    it 'should replace a header value with a block result' do
      mock_headers = mock('HeadersWrapper')
      subject.should_receive(:headers).twice.and_return(mock_headers)
      mock_headers.should_receive(:"[]").with('X-Header').and_return(['old_value', 'old_value'])
      mock_headers.should_receive(:"[]=").with('X-Header', ['new_value', 'new_value'])
      
      i = 0
      subject.modify_header 'X-Header' do |value|
        value.should == "old_value"
        i += 1
        'new_value'
      end
      i.should == 2      
    end
  end
  
  describe "#create_address" do
    before do
      subject.stub!(:sip_factory => mock_sip_factory)
    end
    
    it "should create a wildcard address" do
      mock_sip_factory.should_receive(:createAddress).twice.with('*').and_return(mock_address)
      
      subject.create_address('*').should == mock_address
      subject.create_address(:*).should == mock_address
    end
    
    it "should set expires on the address" do
      mock_sip_factory.should_receive(:createAddress).with('test').and_return(mock_address)
      mock_address.should_receive(:setExpires).with(1234)
      
      subject.create_address('test', :expires => 1234).should == mock_address
    end
    
    it "should set displayName on the address" do
      mock_sip_factory.should_receive(:createAddress).with('test').and_return(mock_address)
      mock_address.should_receive(:setDisplayName).with("display name")
      
      subject.create_address('test', :display_name => "display name").should == mock_address
    end
  end
  
  describe "#create_uri" do
    before do
      subject.stub!(:sip_factory => mock_sip_factory)
      mock_sip_factory.should_receive(:createURI).with('some_uri').and_return(mock_uri)
    end
    
    it "should createURI and set LR to true" do
      mock_uri.should_receive(:setLrParam).with(true)
      mock_uri.should_not_receive(:setParameter)

      subject.create_uri('some_uri').should == mock_uri
    end

    it "should createURI and set LR to false" do
      mock_uri.should_receive(:setLrParam).with(false)
      mock_uri.should_not_receive(:setParameter)

      subject.create_uri('some_uri', :lr => false).should == mock_uri
    end

    it "should createURI, set LR to false and other parameters" do
      mock_uri.should_receive(:setLrParam).with(false)
      mock_uri.should_receive(:setParameter).with("a_name", "a value")
      mock_uri.should_receive(:setParameter).with("another_name", "another value")

      subject.create_uri('some_uri', 
                         :lr => false, 
                         :a_name => "a value", 
                         "another_name" => :"another value").should == mock_uri
    end
  end

  describe "#send_response" do    
    it 'should raise an ArgumentError when call with an incorrect symbol' do
      lambda { subject.send_response(:toto) }.should raise_exception(ArgumentError)
    end
    
    it 'should create a 404 status code when called with :not_found' do
      subject.message.should_receive(:createResponse).with(404).and_return(mock_response)  
      mock_response.should_receive(:send)
      
      subject.send_response(:not_found)
    end
    
    it 'should respond to send_response with Integer' do
      subject.message.should_receive(:createResponse).with(500).and_return(mock_response)  
      mock_response.should_receive(:send)
      
      subject.send_response(500)
    end
    
    it 'should respond to send_response with a block' do
      subject.message.should_receive(:createResponse).with(500).and_return(mock_response)  
      mock_response.should_receive(:addHeader).with('Test1', 'Value1')
      mock_response.should_receive(:send)
      
      subject.send_response(500) do |response|
        response.addHeader('Test1', 'Value1')
      end
    end
    
    it 'should respond to send_response with a Hash' do
      subject.message.should_receive(:createResponse).with(500).and_return(mock_response)  
      mock_response.should_receive(:addHeader).with('Test1', '1234')
      mock_response.should_receive(:send)
      
      subject.send_response 500, :Test1 => 1234
    end
    
    it 'should respond to send_response with a Hash and block' do
      subject.message.should_receive(:createResponse).with(500).and_return(mock_response)  
      mock_response.should_receive(:addHeader).with('Test1', 'Value1')
      mock_response.should_receive(:addHeader).with('Test2', 'Value2')
      mock_response.should_receive(:send)
      
      subject.send_response 500, :Test1 => 'Value1' do |response|
        response.addHeader('Test2', 'Value2')
      end
    end
    
    it 'should respond to send_response with a message and block' do
      subject.message.should_receive(:createResponse).with(500, 'Error').and_return(mock_response)
      mock_response.should_receive(:addHeader).with('Test2', 'Value2')
      mock_response.should_receive(:send)
      
      subject.send_response(500, 'Error') do |response|
        response.addHeader('Test2', 'Value2')
      end      
    end
  end
  
  it 'should respond to header[]' do
    subject.message.should_receive(:getHeader).twice.with('toto').and_return('test1')
    
    subject.header[:toto].should  == 'test1'
    subject.header['toto'].should == 'test1'
  end
  
  it 'should respond to header[]=' do
    subject.message.should_receive(:setHeader).twice.with('toto', 'test2')
    
    subject.header[:toto]  = 'test2'
    subject.header['toto'] = 'test2'
  end
  
  it 'should respond to headers[]' do
    subject.message.should_receive(:getHeaders).twice.with('toto').and_return(['test1', 'test2'])
    
    subject.headers[:toto].should == ['test1', 'test2']
    subject.headers['toto'].should == ['test1', 'test2']
  end
  
  it 'should respond to headers[]=' do
    values = ['foo', :bar]
    
    subject.message.should_receive(:removeHeader).with('toto')
    values.each do |value| 
      subject.message.should_receive(:addHeader).with('toto', value.to_s) 
    end
    subject.message.should_receive(:removeHeader).with('toto')
    values.each do |value| 
      subject.message.should_receive(:addHeader).with('toto', value.to_s) 
    end

    subject.headers[:toto] = values
    subject.headers['toto'] = values
  end
  
  it 'should respond to address_header[]' do
    subject.message.should_receive(:getAddressHeader).twice.with('toto').and_return(['test1', 'test2'])
    
    subject.address_header[:toto].should == ['test1', 'test2']
    subject.address_header['toto'].should == ['test1', 'test2']
  end
  
  it 'should respond to address_header[]=' do
    subject.message.should_receive(:setAddressHeader).twice.with('toto', 'test2')
    
    subject.address_header[:toto]  = 'test2'
    subject.address_header['toto'] = 'test2'
  end
  
  it 'should respond to address_headers[]' do
    subject.message.should_receive(:getAddressHeaders).twice.with('toto').and_return(['test1', 'test2'])
    
    subject.address_headers[:toto].should == ['test1', 'test2']
    subject.address_headers['toto'].should == ['test1', 'test2']
  end
  
  it 'should not respond to address_headers[]=' do
    values = ['foo', :bar]
    subject.message.should_receive(:removeHeader).with('toto')
    values.each do |value| 
      subject.message.should_receive(:addAddressHeader).with('toto', value, true) 
    end
    subject.message.should_receive(:removeHeader).with('toto')
    values.each do |value| 
      subject.message.should_receive(:addAddressHeader).with('toto', value, true) 
    end  
    
    subject.address_headers[:toto] = values
    subject.address_headers['toto'] = values
  end
  
  it 'should respond to header?' do
    subject.message.should_receive(:getHeader).twice.with('toto').and_return('test1')
    
    subject.header?(:toto).should == true
    subject.header?('toto').should == true
  end
  
  it 'should add a header' do
    subject.message.should_receive(:addHeader).twice.with('toto', 'test2')
    
    subject.add_header(:toto, 'test2')
    subject.add_header('toto', 'test2')
  end
  
  it 'should add an address header' do
    subject.message.should_receive(:addAddressHeader).with('titi', 'test1', false)
    subject.message.should_receive(:addAddressHeader).twice.with('toto', 'test2', true)
    
    subject.add_address_header(:titi, 'test1', false)
    subject.add_address_header(:toto, 'test2')
    subject.add_address_header('toto', 'test2')
  end
  
  it 'should push a route' do
    mock_sip_factory.should_receive(:createAddress).with('sip:an_address').and_return(mock_address)
    subject.should_receive(:sip_factory).and_return(mock_sip_factory)
    subject.message.should_receive(:pushRoute).with(mock_address)
    
    subject.push_route('sip:an_address')
  end
  
  describe "#proxy" do
    it 'should proxy without URI' do
      subject.message.should_receive(:requestURI).and_return('the_uri')
      subject.message.should_receive(:proxy).and_return(mock_proxy)
      mock_proxy.should_receive(:proxyTo).with('the_uri')
      
      subject.proxy
    end
    
    it 'should proxy (RR) without URI' do
      subject.message.should_receive(:requestURI).and_return('the_uri')
      subject.message.should_receive(:proxy).and_return(mock_proxy)
      mock_proxy.should_receive(:setRecordRoute).with(true)
      mock_proxy.should_receive(:proxyTo).with('the_uri')
      
      subject.proxy :record_route => true
    end
    
    it 'should proxy with an URI' do
      subject.message.should_receive(:proxy).and_return(mock_proxy)
      subject.should_receive(:sip_factory).and_return(mock_sip_factory)
      mock_sip_factory.should_receive(:createURI).with('the_uri_string').and_return('the_uri')
      mock_proxy.should_receive(:proxyTo).with('the_uri')
      
      subject.proxy('the_uri_string')
    end 
    
    it 'should proxy (RR) with an URI' do
      subject.message.should_receive(:proxy).and_return(mock_proxy)
      subject.should_receive(:sip_factory).and_return(mock_sip_factory)
      mock_sip_factory.should_receive(:createURI).with('the_uri_string').and_return('the_uri')
      mock_proxy.should_receive(:setRecordRoute).with(false)
      mock_proxy.should_receive(:proxyTo).with('the_uri')
      
      subject.proxy('the_uri_string', :record_route => false)
    end 
  end
end
