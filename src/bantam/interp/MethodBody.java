/*
 * File: MethodBody.java
 * CS461 Project 5 First Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/30/17
 */

package bantam.interp;

import bantam.ast.ExprList;

import java.util.List;


public interface MethodBody {

    public Object execute(ExprList actualParams);


}
