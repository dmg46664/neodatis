/**
 * 
 */
package jdbm;

import java.util.Date;

/**
 * @author olivier
 *
 */
public class Human {
	String name;
	int type;
	Date date;
	public Human(String name, int type, Date date) {
		super();
		this.name = name;
		this.type = type;
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Override
	public String toString() {
		return name + " | "+ date;
	}

}
