package com.gmail.mrmioxin.kbemp;

import com.gmail.mrmioxin.kbemp.wwwService.wwwService;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by palchuk on 29.01.2018.
 */
public  class Card {
    protected String id;
    protected String name;
    protected String parent;
    protected Boolean hasChild;
    protected String phone;
    protected String mobile;
    protected Integer tabnum;
    protected String avatar;
    protected String grade;

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

    private static Mnems mnemonics = new Mnems(new String[]{"nbsp", " ", "lt", "<", "gt", ">", "amp", "&","raquo","\"","laquo","\""});

    private static String findPattern(Pattern p, String t, int group){
        Matcher m;
        m = p.matcher(t);
        return (m.find())?m.group(group): "";
    }

    private static String findPattern(Pattern p, String t){
        return findPattern(p, t, 1);
    }

    private static String cleanMnemonic(String s){
        Pattern p = Pattern.compile("\\&(\\p{Alpha}*)\\;");
        Matcher m = p.matcher(s);
        StringBuffer buffer = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(buffer, mnemonics.get(m.group(1)));
        } // while
        m.appendTail(buffer);
        return buffer.toString();
    }

    public Card(JsonObject json) {
        Pattern p_tabn = Pattern.compile("(?si)opencard\\(\\'(\\d+)\\'");
        Pattern p_dol = Pattern.compile("class\\=\\\"s_4\\\"\\>(.+)\\<\\/td");
        Pattern p_fio = Pattern.compile("class\\=\\\"s_1\\\"\\>(\\W+\\s\\W+)\\<\\/td");
        Pattern p_vn = Pattern.compile("\\<b\\>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})");
        Pattern p_sot = Pattern.compile("(\\+\\d \\(\\d\\d\\d\\) \\d\\d\\d-\\d\\d-\\d\\d)");
        Pattern p_ava = Pattern.compile("img src\\=\\\"(http(s|)://|)([\\s\\S]+)\\\" wi");

        String text = json.getAsJsonPrimitive("text").getAsString();
        this.id = json.getAsJsonPrimitive("id").getAsString();//Integer.getInteger(json.getAsString("id"));
        this.parent = json.getAsJsonPrimitive("parent").getAsString();
        this.hasChild = json.getAsJsonPrimitive("children").getAsBoolean();
        String stabnum ="";
        if (hasChild) {
            this.name = cleanMnemonic(text);
        } else {
            stabnum = findPattern(p_tabn,text);
            this.tabnum = new Integer((stabnum.equals(""))? "0" :stabnum);
            this.name = findPattern(p_fio, text);
            this.phone = findPattern(p_vn, text);
            this.mobile = findPattern(p_sot, text);
            this.avatar = findPattern(p_ava, text, 3);
            this.grade = findPattern(p_dol,text);
        }
    }

    public Card(String str) {
        String[] ac;
        ac = str.split("\\t");
        ArrayList<String> namephone= new ArrayList<>();
        this.parent = ac[1];

        if (ac.length > 3){
            this.tabnum = new Integer(ac[0]);
            this.phone = ac[4];
            namephone.add(ac[3]);
            namephone.add(ac[4]);
            try {
                this.name = cleanMnemonic ((new wwwService()).getApidata().getO(namephone));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            this.mobile = ac[5];
            this.avatar = "www-int/"+ac[6];
            this.grade = ac[2];

        } else {
            this.hasChild = true;
            this.id = ac[0];
            this.name = cleanMnemonic(ac[2]);

        }
    }

    public Card() {
    }

    public String compareCard(Card c){
        String ret = "";
        if (!this.parent.equals(c.parent)) { ret += "was change parent;";}
        if (!this.phone.equals(c.phone)) { ret += "was change internal phone;";}
        if (!this.mobile.equals(c.mobile)) { ret += "was change mobile phone;";}
        //if (this.tabnum != c.tabnum) { ret += "was change tabnum;";}
        if (!this.avatar.equals(c.avatar)) { ret += "was change avatar image;";}
        if (!this.grade.equals(c.grade)) { ret += "was change grade;";}
        return ret;
    }
    public ArrayList<String> getNamePhone() {
        ArrayList<String> a = new ArrayList<>();
        a.add(this.name);
        a.add(this.phone);
        return a;
    }

    public void updatename (String newname){ // добавляем отчество
        this.name = newname;
    }

    public Boolean isParent(){
        return this.hasChild;
    }

    public String get_id(){
        return this.id;
    }

    @Override
    public String toString() {
        return "Card{" +
            "id=" + id +
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
        return id + delimiter +
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
        map.put("name", name);
        map.put("parent",parent);
        map.put("phone",phone);
        map.put("mobile",mobile);
        map.put("tabnum",tabnum.toString());
        map.put("grade",grade);
        map.put("avatar",avatar);
        return map;
    }
}
