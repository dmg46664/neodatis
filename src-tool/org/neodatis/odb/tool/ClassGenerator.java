/**
 * 
 */
package org.neodatis.odb.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.ODB;
import org.neodatis.odb.core.layers.layer2.meta.ClassAttributeInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer4.engine.Dummy;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.OdbClassUtil;
import org.neodatis.tool.wrappers.list.IOdbList;

/**
 * @author olivier
 *
 */
public class ClassGenerator {
	
	public void genereateClasses(String sourceFolder, ODB odb) throws IOException{
		DLogger.info(String.format("Generating classes to %s",sourceFolder));
		MetaModel metaModel = Dummy.getEngine(odb).getSession().getMetaModel();
		
		Collection<ClassInfo> cis = metaModel.getUserClasses();
		
		Iterator<ClassInfo> iterator = cis.iterator();
		
		while(iterator.hasNext()){
			generateOneClass(sourceFolder, iterator.next());
		}
	}

	/**
	 * @param next
	 * @throws IOException 
	 */
	private void generateOneClass(String sourceFolder, ClassInfo ci) throws IOException {
		
		if(ci.getFullClassName().startsWith("java.") || ci.getFullClassName().startsWith("sun.")){
			return;
		}

		
		DLogger.info(String.format("Generating class for %s", ci.getFullClassName()));
		IOdbList<ClassAttributeInfo> cais = ci.getAttributes();
		StringBuffer buffer = new StringBuffer();
		buffer.append("// Generated by NeoDatis on ").append(new Date()).append("\n");
		buffer.append(String.format("package %s;\n\n",OdbClassUtil.getPackageName(ci.getFullClassName())));
		buffer.append(String.format("public class %s{\n\n",OdbClassUtil.getClassName(ci.getFullClassName())));
		StringBuffer gettersAndSetters = new StringBuffer();
		
		for(int i=0;i<cais.size();i++){
			ClassAttributeInfo cai = cais.get(i);
			String name = capitalize(cai.getName(),true);
			
			
			buffer.append(String.format("\tprotected %s %s;\n",cai.getAttributeType().getName(),cai.getName()));
			

			gettersAndSetters.append(String.format("\tpublic %s get%s(){\n\t\treturn %s;\n\t}\n\n", cai.getAttributeType().getName(), name, cai.getName()));
			gettersAndSetters.append(String.format("\tpublic void set%s(%s %s){\n\t\tthis.%s = %s;\n\t}\n\n",name, cai.getAttributeType().getName(), cai.getName(), cai.getName(),cai.getName()));
			
		}
		buffer.append("\n\n");
		buffer.append(gettersAndSetters);
		buffer.append(String.format("}\n"));
		String fileName = String.format("%s/%s.java", sourceFolder, ci.getFullClassName().replace(".", "/"));
		new File(fileName).getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(fileName);
		out.write(buffer.toString().getBytes());
		out.close();
		DLogger.info(String.format("File %s created for class %s",fileName, ci.getFullClassName()));
	}
	
	public static void main(String[] args) throws IOException {
		ODB odb = NeoDatis.open("/Users/olivier/MyApps/Gyowanny/eseller_db_new.odb", NeoDatis.getConfig().setCheckMetaModelCompatibility(false));
		ClassGenerator generator = new ClassGenerator();
		generator.genereateClasses("/Users/olivier/Data/Eclipse/WorkSpace/NeoDatis/Test/src" , odb);
	}


	/**
	 * Method used to capitalize string and remove '_'
	 * 
	 * @example StringUtils.capitalize("CLIENT_NAME" , true) return clientName
	 * @example StringUtils.capitalize("CLIENT_NAME" , false) return ClientName
	 * @param string
	 *            The string to capitalize
	 * @param firstOneIsLowerCase
	 *            true if first char must be lower case, false if must be upper
	 *            case
	 * @return The new String
	 */
	public String capitalize(String string, boolean firstOneIsLowerCase) {
		StringBuffer newString = new StringBuffer();
		char c = 0;

		for (int i = 0; i < string.length(); i++) {
			c = string.charAt(i);
			String s = ""+c;
			if(i==0){
				s = s.toUpperCase();
			}
			newString.append(s);
		}
		return newString.toString();
	}

}
