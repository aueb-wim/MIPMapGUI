/* Copyright 2015-2016 by the Athens University of Economics and Business (AUEB).

   This file is part of MIPMap.

   MIPMap is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MIPMap is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MIPMap.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unibas.spicygui.vista.intermediatezone;

//giannisk
public class FunctionProperties {
    public static final String[][] functionArray = {
        {"Abs","Returns the absolute value of same type for Numbers / returns the Modulus for Complex","abs(x)"},
        {"Aggregate Function","Execute an aggregation function","aggregation(function,value,\"group_col\",\"where_col1==val1\")"},
        {"Append","Appends second parameter to first paramenter as string","append(str1, str2)"},
        {"Arc Cosine","Inverse Cosine","acos(x)"},
        {"Arc Cosine Hyperbolic","Inverse Hyperbolic Cosine","acosh(x)"},
        {"Arc Sine","Inverse Sine","asin(x)"},
        {"Arc Sine Hyperbolic","Inverse Hyperbolic Sine","asinh(x)"},
        {"Arc Tangent","Inverse Tangent","atan(x)"},
        {"Arc Tangent(2 parameters)","Inverse Tangent with 2 parameters","atan2(y,x)"},
        {"Arc Tangent Hyperbolic","Inverse Hyperbolic Tangent","atanh(x)"},
        {"Argument", "Argument of a complex number ", "arg(c)"},
        {"Binomial","Binomial coefficients","binom(n,i)"},
        {"Ceiling","The smallest integer above the number","ceil(x)"},
        {"Complex PFMC","Converts a pair of real numbers to a complex number","complex(x,y)"},
        {"Complex conjugate","The complex conjugate of a number","conj(c)"},
        {"Contains","Returns true if str contains the subStr at least once","contains(str, subStr)"},
        {"ContainCount","Returns the number of occurrences of subStr within str","containCount(str, subStr)"},
        {"Cosine","Cosine","cos(x)"},
        {"Cosine Hyperbolic","Hyperbolic Cosine","cosh(x)"},
        {"Current Year","Returns current year","currentYear()"},
        {"Date","Returns current date","date()"},
        {"DateTime","Returns current date in datetime format","datetime()"},
        {"Exponential","The result of the exponential function (e^x)","exp(x)"},
        {"Floor","The smallest integer below the number","floor(x)"},
        {"Func Generator","Gets a function from an input file","funcGenerator(field,field)"},
        {"If","The if function; trueval will be returned if cond is >0 or True and falseval will be returned if cond is <= 0 or False","if(cond, trueval, falseval)"},
        {"Imaginary","Imaginary Component","im(c)"},
        {"Index Of","Returns the position of the first occurrence of a specified value in a string","indexof(str, subStr)"},
        {"Is Not Null","Tests if the argument is not null","isNotNull(arg)"},
        {"Is Null","Tests if the argument is null","isNull(arg)"},
        {"Is Numeric","Tests if the argument is numeric","isNumeric(str)"},
        {"Length","Returns the length of a string","len(str)"},
        {"Log","Logarithm base 10","log(x)"},
        {"Ln","Natural Logarithm","ln(x)"},
        {"Modulus","Calculates the modulus x % y of the arguments","mod(x,y)"},
        {"New Id","Gets the next number in a sequence","newId()"},
        {"Null","Returns null value","null()"},
        {"Polar","Constructs a complex number from modulus and argument","polar(r, theta)"},
        {"Power","Computes the power of an number","pow(x,y)"},
        {"Real","Real Component","re(c)"},
        {"Replace","In the first argument, find all occurences of the second argument and replace them with the third one ","replace(str, text1, text2)"},
        {"Round","The closest integer to the argument. The second argument is optional and refers to decimal places","round(x,[y])"},
        {"Sine","Sine","sin(x)"},
        {"Sine Hyperbolic","Hyperbolic Sine","sinh(x)"},
        {"Square Root","The square root of the parameter","sqrt(x)"},
        {"Substring","Extract substring of first argument. Second argument is starting index, third argument is optional and it is the ending index","substring(str, start, [end])"},
        {"Tangent","Tangent","tan(x)"},
        {"Tangent Hyperbolic","Hyperbolic Tangent","tanh(x)"},
        {"To Date","Converts a string to date format given a specific template pattern, e.g. \"MM/DD/YYYY\", for the input data, provided by the user","todate(str, pattern)"},
        {"To Double","Returns the double value of a number","todouble(x)"},
        {"To Integer","Returns the integer value of a number","toint(x)"},
        {"To Lowercase","Converts a string to lowercase letters","tolower(str)"},
        {"To String","Converts a data type to text","tostring(data)"},
        {"To Uppercase","Converts a string to uppercase letters","toupper(str)"}
    };
    
}
