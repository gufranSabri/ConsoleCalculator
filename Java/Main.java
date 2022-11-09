import java.util.*;

public class Main {
    public static void main(String[] args) {
        Calculator.inputProcessor();
    }
}

class Calculator{
    

    public static void inputProcessor(){
        HashMap<String, String> variables = new HashMap<>();
        ArrayList<String> keywords = new ArrayList<>(Arrays.asList("write", "del", "var"));

        while(true){
            System.out.print("\n>");
            String input = new Scanner(System.in).nextLine();

            if(input.equals("q"))break;
            
            if(input.startsWith("write ")||input.startsWith("var")||input.startsWith("del ")){
                String[] sa = input.split(" ");
                if(sa[0].equals("var"))System.out.println(variables);
                else {
                    if(variables.containsKey(sa[1])){
                        if(sa[0].equals("write"))System.out.println(sa[1] + " = " + variables.get(sa[1]));
                        else{
                            System.out.println("deleted");
                            variables.remove(sa[1]);
                        }
                    }
                    else System.out.println("Invalid expression");
                }
            }
            else if(input.contains("=")){
                String[] sa = input.split("=");
                if(sa.length!=2){
                    System.out.println("Invalid expression");
                    continue;
                }
                else if(!sa[0].matches("[a-z]+[a-z1-9]*")){
                    System.out.println("Invalid identifier at index 0");
                    continue;
                }
                else if(keywords.contains(sa[0])){
                    System.out.println("Cannot use keywords del, var and write as identifiers");
                    continue;
                }
                
                boolean flag = false;
                for (String key : variables.keySet())sa[1] = sa[1].replace(key, variables.get(key));
                for (int i = 0; i < sa[1].length(); i++)
                    if(!sa[1].substring(i,i+1).matches("[-+)(*^/0-9.]")){
                        System.out.println("Invalid expression. Possible use of uninitialized variable");
                        flag = true;
                        break;
                    }
                if(flag)continue;
                        
                String answer = evaluator(infixToPostfix(tokenizer("(" + sa[1] + ")"))).toString();
                try{variables.put(sa[0], Double.parseDouble(answer)+"");}
                finally{System.out.println(answer);}
            }
            else{
                boolean flag = false;
                for (String key : variables.keySet())input = input.replace(key, variables.get(key));
                for (int i = 0; i < input.length(); i++) 
                    if(!input.substring(i,i+1).matches("[-+)(*^/0-9.]")){
                        System.out.println("Invalid expression. Possible use of uninitialized variable");
                        flag=true;
                        break;
                    }
                if(flag)continue;
                System.out.println(evaluator(infixToPostfix(tokenizer("(" + input + ")"))));
            }
        }
    }

    public static Response evaluator(Response postfix){
        if(!postfix.response.equals(Response.SUCCESS_RESPONSE))return postfix;
        
        Response res = new Response();
        for(Token s:postfix.stack){
            if(s.token.matches("[0-9.]+|[-][0-9.]+")) res.stack.push(s);
            else {
                try {
                    String num2=res.stack.pop().token, num1 = res.stack.pop().token;
                    double x = biOperandCalculator(num1, s.token, num2);
                    res.stack.push(new Token(-1,x+""));
                }
                catch(Exception e){ 
                    return res.registerExtraOperator(s.index);
                }
            }
        }
        if(res.stack.size()!=1) return res.registerExtraOperand();
        return res.registerSuccess();
    }
    public static Response infixToPostfix(Response infix){
        if(!infix.response.equals(Response.SUCCESS_RESPONSE))return infix;

        Stack<Token> stack= new Stack<>();
        Response res = new Response();
        
        try{
            for (Token x : infix.stack) {
                if (x.token.equals("(")) stack.push(x);
                else if (x.token.equals(")")) {
                    while (stack.peek().token.matches("[-^+/*]")) res.stack.push(stack.pop());
                    stack.pop();
                }
                else if (x.token.matches("[0-9.]+|[-][0-9.]+")) res.stack.add(x);
                else if (x.token.matches("[-+/*^]")) {
                    if(x.token.matches("[-+]")) while(stack.peek().token.matches("[-+/*^]")) res.stack.push(stack.pop()); 
                    if(x.token.matches("[/*]")) while(stack.peek().token.matches("[/*^]")) res.stack.push(stack.pop());
                    stack.push(x);
                }
            }
        }
        catch(Exception e){return res.registerInvalidExpression();}

        return res.registerSuccess();
    }
    public static Response tokenizer(String s){
        Response res = new Response();
        s=s.replace(" ", "");

        for (int i = 0; i <s.length() ; i++) {
            if(s.substring(i,i+1).matches("[0-9.]+")){
                if(res.stack.isEmpty()||!res.stack.peek().token.matches("[0-9.]+")) res.stack.add(new Token(i, s.substring(i,i+1)));
                else{
                    String temp=res.stack.pop().token;
                    if(temp.contains(".")&&s.substring(i, i+1).equals(".")) return res.registerInvalidToken(s, temp);
                    res.stack.add(new Token(i,temp+s.substring(i,i+1)));
                }
            }
            else if(s.substring(i,i+1).matches("[-+/*)(^]")) {
                if(s.substring(i,i+1).equals("-")){
                    if(s.substring(i+1,i+2).matches("[0-9]")) {
                        if(!res.stack.peek().token.matches("[-+/*^(]")&&!res.stack.isEmpty()) { 
                            res.stack.add(new Token(i, "-"));
                            continue; 
                        }
                        int x = i++;
                        boolean foundDot = false;
                        while (s.substring(i, i + 1).matches("[0-9.]")) {
                            if(s.substring(i, i + 1).equals(".")){
                                if(foundDot)return res.registerInvalidToken(s, s.substring(x,x+1));
                                foundDot=true;
                            }
                            i++;
                        }
                        res.stack.add(new Token(x,s.substring(x,i)));
                        i--;
                        continue;
                    }
                }
                res.stack.add(new Token(i,s.substring(i, i + 1)));
            }
            else return res.registerInvalidToken(s, s.substring(i, i+1));
        }
        return res.registerSuccess();
    }
    public static double biOperandCalculator(String num1, String operator, String num2) {
        switch (operator) {
            case "+": return Double.parseDouble(num1) + Double.parseDouble(num2);
            case "-": return Double.parseDouble(num1) - Double.parseDouble(num2);
            case "*": return Double.parseDouble(num1) * Double.parseDouble(num2);
            case "/": return Double.parseDouble(num1) / Double.parseDouble(num2);
            case "^": return Math.pow(Double.parseDouble(num1),Double.parseDouble(num2));
        }
        return 0;
    }
}


class Response{
    static String SUCCESS_RESPONSE = "success";

    Stack<Token> stack = new Stack<>();
    String response="";

    public Response registerSuccess(){
        response = SUCCESS_RESPONSE;
        return this;
    }

    public Response registerInvalidToken(String s, String invalidToken){
        response = "Invalid token starting at index " + (s.indexOf(invalidToken)-1);
        return this;
    }

    public Response registerExtraOperator(int index){
        response = "Extra operator at index " + (index-1);
        return this;
    }

    public Response registerExtraOperand(){
        response = "Invalid number of operands";
        return this;
    }

    public Response registerInvalidExpression(){
        response = "Invalid expression";
        return this;
    }

    public String toString() {
        if(response.equals(SUCCESS_RESPONSE))return stack.peek().toString();
        return response;
    }
}

class Token{
    int index = 0;
    String token = "";

    Token(int index, String token){
        this.token=token;
        this.index=index;
    }
    public String toString() {return token;}
}

// javac --release 8 Main.java && java Main