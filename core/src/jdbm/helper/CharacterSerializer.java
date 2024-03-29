/**
 * JDBM LICENSE v1.00
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "JDBM" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Cees de Groot.  For written permission,
 *    please contact cg@cdegroot.com.
 *
 * 4. Products derived from this Software may not be called "JDBM"
 *    nor may "JDBM" appear in their names without prior written
 *    permission of Cees de Groot.
 *
 * 5. Due credit should be given to the JDBM Project
 *    (http://jdbm.sourceforge.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) Alex Boisvert. All Rights Reserved.
 * Contributions are Copyright (C) 2001 by their associated contributors.
 *
 */
/*
 * Created on May 9, 2006
 */
package jdbm.helper;

import java.io.IOException;

import jdbm.extser.Stateless;




/**
 * {@link Character} serializer.
 *  
 * @version $Id: CharacterSerializer.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public final class CharacterSerializer implements Serializer<Character>, Stateless
{

    public CharacterSerializer() {}
    
    public byte[] serialize(Character obj) throws IOException {
        char c = obj.charValue();
        return Conversion.convertToByteArray((short)c);
//        char c = ((Character) obj).charValue();
//        String s = new String(""+c);
//        return StringSerializer.INSTANCE.serialize(s);
    }
    
    public Character deserialize(byte[] data) throws IOException {
        return new Character( (char) Conversion.convertToShort(data) );
//        String s = (String) StringSerializer.INSTANCE.deserialize(data);
//        return new Character(s.charAt(0));
    }

    private static final long serialVersionUID = 1894988272958857593L;
    
}