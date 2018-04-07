package com.serendipity.chengzhengqian.jsos;

public class Command {
    public String code;
    public int state;
    public boolean useBabel;
    public int id=0;
    public static int stop=0;
    public static int running=1;
    public static int hint=2;
    public Command(String code){
        this.code=code;this.state=running;
    }
    public void setCommand(String code){
        this.code=code;
    }
    public void setId(int id){this.id=id;};
    public String[] parsedFrom;
    public String hintResult;
    public void setHint(String[] parsedForm){this.state=hint;this.parsedFrom=parsedForm;}
}
