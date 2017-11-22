/*****************************************************************************

 JEP 2.4.1, Extensions 1.1.1
      April 30 2007
      (c) Copyright 2007, Nathan Funk and Richard Morris
      See LICENSE-*.txt for license information.

*****************************************************************************/

package org.nfunk.jep.function;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.nfunk.jep.*;

public class MD5 extends PostfixMathCommand
{
	public MD5()
	{
		numberOfParameters = 1;
	}
	
	public void run(Stack inStack)
		throws ParseException 
	{
		checkStack(inStack);// check the stack
		Object strToHash = inStack.pop();
                
		inStack.push(md5(strToHash.toString()));//push the result on the inStack
		return;
	}

	public Object md5(Object strToHash)
	{
		String generatedHash = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(strToHash.toString().getBytes());
			byte[] bytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i=0; i < bytes.length; i++) {
//				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100 ).substring(1)); 
			    byte b = bytes[ i ];
			    String hex = Integer.toHexString((int) 0x00FF & b);
			    if (hex.length() == 1) {
		    		sb.append("0");
			    }
			    sb.append( hex );
			}
			generatedHash = sb.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedHash;
        }
}
