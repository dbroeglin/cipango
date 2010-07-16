package org.cipango.diameter.ims;

public class TBCD
{
    private String _number;
    
    public TBCD(String number) throws IllegalArgumentException
    {
        for (int i = 0; i < number.length(); i++)
        {
            char c = number.charAt(i);
            if (!Character.isDigit(c))
                throw new IllegalArgumentException("Non digit char in: " + number);
        }
        _number = number;
    }
    
    public byte[] getBytes()
    {   
        byte[] tbcd = new byte[(_number.length()+1)/2];
        
        for (int i = 0; i < tbcd.length; i++)
        {
            int high = Character.digit(_number.charAt(2*i), 10);
            int low = ((2*i+1) >= _number.length()) ? 0xF : Character.digit(_number.charAt(2*i+1), 10);
            
            tbcd[i] = (byte) (high << 4 | low);
        }
        return tbcd;
    }
    
    public static void main(String[] args)
    {
       byte[] b = (new TBCD("123").getBytes());
       System.out.println(b);
    }
    
}
