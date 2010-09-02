require File.dirname(__FILE__) + '/helper'

describe Sipatra do
  subject do
    Sipatra
  end
  
  it_should_behave_like ExtensionsMethods
end
