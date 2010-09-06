require File.dirname(__FILE__) + '/helper'

describe Sipatra do
  subject do
    Sipatra
  end
  
  it_should_behave_like ExtensionsMethods
  
  describe "#register_extensions" do
    module ExtensionModule
      def dsl_extension
        invite 'foo' do
          # nothing
        end
      end
    end

    it "should add the module as a DSL extensions" do
      Sipatra.register_extension ExtensionModule
      
      Sipatra::Application.public_methods.include?('dsl_extension').should be_true
      private_methods.include?('dsl_extension').should be_true
    end
  end
end
