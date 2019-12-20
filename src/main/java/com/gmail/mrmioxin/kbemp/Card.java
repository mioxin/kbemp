package com.gmail.mrmioxin.kbemp;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by palchuk on 29.01.2018.
 */
public  class Card {
    private Logger logger =  BaseConst.logg;
    protected String idr;
    protected String name;
    protected String parent;
    protected Long parentid;
    protected Boolean hasChild;
    protected String phone;
    protected String mobile;
    protected Integer tabnum;
    protected String avatar;
    protected String grade;
    protected String parentname;

    private static class Mnems {
        static Map<String, String> subst = new HashMap<>();
        public Mnems(String... args) {
            for (int i = 0; i < args.length / 2; i++) {
                subst.put(args[i * 2], args[i * 2 + 1]);
            } // for
        } // Mnems
        public String get(String key) {
            String repl = subst.get(key);
            return repl != null ? repl : '&' + key + ';';
        } // get
    } // Mnems

    private static Mnems mnemonics = new Mnems(new String[]{"nbsp", " ", "lt", "<", "gt", ">", "amp", "&","raquo","\"","laquo","\"","quot","\""});

    public Card(JsonObject json) {
        Pattern p_tabn = Pattern.compile("(?si)opencard\\(\\'(\\d+)\\'");
        Pattern p_dol = Pattern.compile("class\\=\\\"s_4\\\"\\>(.+)\\<\\/td");
        Pattern p_fio = Pattern.compile("class\\=\\\"s_1\\\"\\>(\\W+\\s\\W+)\\<\\/td");
        Pattern p_vn = Pattern.compile("\\<b\\>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})");
        Pattern p_sot = Pattern.compile("(\\+\\d \\(\\d\\d\\d\\) \\d\\d\\d-\\d\\d-\\d\\d)");
        Pattern p_ava = Pattern.compile("img src\\=\\\"(http(s|)://|)([\\s\\S]+)\\\" wi");

        String text = json.getAsJsonPrimitive("text").getAsString();
        this.idr = json.getAsJsonPrimitive("id").getAsString();//Integer.getInteger(json.getAsString("id"));
        this.parent = json.getAsJsonPrimitive("parent").getAsString();
        this.hasChild = json.getAsJsonPrimitive("children").getAsBoolean();
        String stabnum ="";
        if (hasChild) {
            this.name = cleanw(text);
        } else {
            stabnum = findPattern(p_tabn,text);
            this.tabnum = new Integer((stabnum.equals(""))? "0" :stabnum);
            this.name = findPattern(p_fio, text);
            this.phone = findPattern(p_vn, text);
            this.mobile = findPattern(p_sot, text);
            this.avatar = findPattern(p_ava, text, 3);
            //удалить из имени файла хвост после знака '?' ("http://hr-filesrv.hq.bc/data/avatars/302716.jpg?1704")
            int posv = this.avatar.indexOf("?");
            if (posv>0) {
                this.avatar = this.avatar.substring(0,posv);
            }

            this.grade = findPattern(p_dol,text);
        }
    }

    public Card(String str) throws IOException {
        String[] ac;
        ac = str.split("\\t");
        if (ac.length <= 1){
            logger.fine("Файл должен содержать разделитель полей табуляцию.");
            throw new IOException();
        }
        ArrayList<String> namephone= new ArrayList<>();
        this.parent = ac[1];
        this.idr = ac[0];

        if (ac[0].indexOf("razd") < 0 ){
            this.phone = ac[4];
            namephone.add(ac[3]);
            namephone.add(ac[4]);
//            try {
                this.tabnum = new Integer(ac[0]);
                this.name = cleanw(ac[3]);//((new wwwService()).getApidata().getO(namephone));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
////            } catch (NumberFormatException e) {
////                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            this.mobile = ac[5];
            this.avatar = "www-int"+ac[6];
            this.grade = ac[2];

        } else {
            this.hasChild = true;
            this.name = cleanw(ac[2]);

        }
    }

    public Card(String idr, String name, String parent, Long pid, Boolean hasChild, String parentname) {
        this.idr = idr;
        this.name = name;
        this.parentid = pid;
        this.parent = parent;
        this.hasChild = hasChild;
        this.parentname = parentname;
    }

    public Card(){}

    public String compareCard(Card c){
        String ret = "";
        if (this.phone != null && !this.phone.equals(c.phone)) { ret += "was change internal phone;";}
        if (this.mobile != null && !this.mobile.equals(c.mobile)) { ret += "was change mobile phone;";}
        //if (this.tabnum != c.tabnum) { ret += "was change tabnum;";}
        if (this.avatar != null && !this.avatar.equals(c.avatar)) { ret += "was change avatar image;";}
        if (this.grade != null && !this.grade.equals(c.grade)) { ret += "was change grade;";}
        if (!this.name.split(" ")[0].equals(c.name.split(" ")[0])) { ret += "was change name;";}
        return ret;
    }

//    public ArrayList<String> getNamePhone() {
//        ArrayList<String> a = new ArrayList<>();
//        a.add(this.name);
//        a.add(this.phone);
//        return a;
//    }

    public void setname(String newname){ // добавляем отчество
        this.name = newname;
    }

    public Boolean isParent(){
        if (this.hasChild == null) {
            return false;
        } else {
            return this.hasChild;
        }
    }

    public String getidr(){
        return this.idr;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public Integer getTabnum() {
        return tabnum;
    }

    public String getparent() {
        return this.parent;
    }
    public Long getparentid() {
        return this.parentid;
    }
    public String getparentname() {
        return this.parentname;
    }

    public void setparentid(Long pid){
        this.parentid = pid;
    }

    private static String findPattern(Pattern p, String t, int group){
        Matcher m;
        m = p.matcher(t);
        return (m.find())?m.group(group): "";
    }

    private static String findPattern(Pattern p, String t){
        return findPattern(p, t, 1);
    }

    private static String cleanw(String s){
        Pattern p = Pattern.compile("\\&(\\w{4,5})\\;");
        Matcher m = p.matcher(s);
        StringBuffer buffer = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(buffer, mnemonics.get(m.group(1)));
        } // while
        m.appendTail(buffer);
        return buffer.toString();
    }
    
    @Override
    public String toString() {
        return "Card{" +
            "idr=" + idr +
            ", name='" + name + '\'' +
            ", parent='" + parent +'\'' +
            ", hasChild=" + hasChild +
            ", phone='" + phone + '\'' +
            ", mobile='" + mobile + '\'' +
            ", tabnum=" + tabnum +
            ", avatar='" + avatar + '\'' +
            ", grade='" + grade + '\'' +
            '}';
    }

    public String toCSV(Character delimiter){
        return idr + delimiter +
            parent + delimiter +
            name + delimiter +
            phone + delimiter +
            mobile + delimiter +
            tabnum + delimiter +
            avatar + delimiter +
            grade + delimiter;
    }
    public String toCSV(){
        return toCSV(';');
    }

    public Map<String,String> toMap() {
        HashMap<String,String> map = new HashMap<>();
        map.put("idr", idr);
        map.put("name", name);
        map.put("parent",parent);
        map.put("phone",phone);
        map.put("mobile",mobile);
        if (tabnum == null) {
            map.put("tabnum", "0");
        } else {
            map.put("tabnum", tabnum.toString());
        }
        if (parentid == null) {
            map.put("parentid", "0");
        } else {
            map.put("parentid", parentid.toString());
        }
        map.put("grade",grade);
        map.put("avatar",avatar);
        return map;
    }
}
