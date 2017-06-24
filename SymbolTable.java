/* Code By:- Pulkit Kumar Dhir 
 * Took all the instructions & discussions into consideration while writing SymbolTable.java  
 * */

package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import cop5556sp17.AST.Dec;

public class SymbolTable 
{

	int current_scope,next_scope;
	Stack<Integer> scope_stack =new Stack<Integer>();
	HashMap<String,ArrayList<SymTable>> hmap=new HashMap<String,ArrayList<SymTable>>();

	public void enterScope()
	{               
		current_scope = ++next_scope;
		scope_stack.push(current_scope);
	}

	public void leaveScope()
	{		
		scope_stack.pop();
		current_scope = scope_stack.peek();
	}

	public boolean insert(String ident, Dec dec)
	{		
		ArrayList<SymTable> alist=new ArrayList<SymTable>();
		SymTable st=new SymTable(current_scope,dec);
		if(hmap.containsKey(ident))
		{
			alist=hmap.get(ident);
			for(SymTable s:alist)
			{
				if(s.scope==current_scope)
					return false;
			}
		}
		alist.add(st);
		hmap.put(ident, alist);
		return true;
	}

	public Dec lookup(String ident)
	{
		if(!hmap.containsKey(ident))
		return null;

		Dec dec=null;
		ArrayList<SymTable> ps = hmap.get(ident);
		for(int i=ps.size()-1;i>=0;i--)
		{
		int temp_scope = ps.get(i).getScope();
		if(scope_stack.contains(temp_scope))
		{
		dec = ps.get(i).getDec();
		break;
		}
		}
		return dec;
		}	
	
	public SymbolTable() 
	{
		this.current_scope=0;
		this.next_scope=0;
		scope_stack.push(0);
	}

	@Override
	public String toString() 
	{
		return this.toString();
	}

	class SymTable
	{
		int scope;
		Dec dec;
		public SymTable(int temp_scope,Dec temp_dec)
		{
			this.scope=temp_scope;
			this.dec=temp_dec;
		}
		public int getScope()
		{
			return scope;
		}
		public Dec getDec()
		{
			return dec;
		}
	}
}
