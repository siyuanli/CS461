/*
 * File: MethodBody.java
 * CS461 Project 6 Second Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 5/10/17
 */

package bantam.interp;

import bantam.ast.ExprList;
/**
 * A Method in our interpreter
 */
public interface MethodBody {

    /**
     * What gets executed when you call a method
     * @param actualParams the parameters of the method
     * @return the return value of the method
     */
    public Object execute(ExprList actualParams);


}
