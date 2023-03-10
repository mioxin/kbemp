package com.gmail.mrmioxin.kbemp;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Date;
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
    private Logger logger = Logger.getLogger(Main.class.getName());// BaseConst.logg;
    protected Date date;
    protected String idr;
    protected String name;
    protected String parent;
    protected Long parentid;
    protected Boolean hasChild;
    protected String phone;
    protected String mobile;
    protected String email;
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
        this.date = new Date(System.currentTimeMillis());
        //Pattern p_tabn = Pattern.compile("(?si)opencard\\(\\'(\\d+)\\'");
        Pattern p_tabn = Pattern.compile("data-tabnum\\=\\\"(\\d+)\\\"");
        Pattern p_dol = Pattern.compile("class\\=\\\"s_4\\\"\\>(.+)\\<\\/td");
        Pattern p_fio = Pattern.compile("class\\=\\\"s_1\\\"\\>(\\W+\\s\\W+)\\<");
        Pattern p_vn = Pattern.compile("\\<b\\>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})");
        Pattern p_sot = Pattern.compile("(\\+\\d\\s*\\(\\d\\d\\d\\)\\s*\\d\\d\\d-\\d\\d-\\d\\d)");
        Pattern p_email = Pattern.compile("\\<a\\s+href\\=\\\"mailto:(\\S+@\\S+\\.kz)\\\"");//<a href="mailto:Mikhail.Palchuk@kaspi.kz"
        Pattern p_ava = Pattern.compile("img src\\=\\\"(http(s|)://|)([\\s\\S]+)\\\" wi");

        String text = json.getAsJsonPrimitive("text").getAsString();
        this.idr = json.getAsJsonPrimitive("id").getAsString();//Integer.getInteger(json.getAsString("id"));
        this.parent = json.getAsJsonPrimitive("parent").getAsString();
        this.hasChild = true; //???? ?????????????????? ???????????????? ?????? ??????????
        JsonElement attr;
        if (json.has("li_attr")) {
            attr = json.get("li_attr");
            if (attr.isJsonObject()) {
                JsonObject json_attr = attr.getAsJsonObject();
                try {
                    this.hasChild = (json_attr.getAsJsonPrimitive("class").getAsString().equals("sotr"))?false:true;
                    //logger.info(json_attr.getAsJsonPrimitive("class").getAsString());
                }
                catch (JsonIOException e) {
                    System.err.println(this.idr + "Failed get class=sotr from li_attr. " + e.getLocalizedMessage());
                    logger.severe(this.idr + "Failed get class='sotr' from li_attr.");
                }
            } else {
                //logger.warning(this.idr + " 'li_attr' is not JsonObject" + json.get("li_attr"));    
            }
        } else {
            //logger.warning(this.idr + " has not 'li_attr'.");
        };
        
        //this.hasChild = json.getAsJsonPrimitive("children").getAsBoolean();
        String stabnum ="";
        if (hasChild) {
            this.name = cleanw(text);
        } else {
            stabnum = findPattern(p_tabn,text);
//            this.tabnum = new Integer((stabnum.equals(""))? "0" :stabnum);
            this.tabnum = Integer.valueOf((stabnum.equals(""))? "0" :stabnum);
            this.name = findPattern(p_fio, text);
            this.phone = findPattern(p_vn, text);
            this.email = findPattern(p_email, text);
            this.mobile = findPattern(p_sot, text);
            this.avatar = findPattern(p_ava, text, 3);
            //?????????????? ???? ?????????? ?????????? ?????????? ?????????? ?????????? '?' ("http://hr-filesrv.hq.bc/data/avatars/302716.jpg?1704")
            int posv = this.avatar.indexOf("?");
            if (posv>0) {
                this.avatar = this.avatar.substring(0,posv);
            }

            this.grade = findPattern(p_dol,text);
        }
    }

    public Card(String str) throws IOException {
        this.date = new Date(System.currentTimeMillis());
        String[] ac;
        ac = str.split("\\t");
        if (ac.length <= 1){
            logger.info("???????? ???????????? ?????????????????? ?????????????????????? ?????????? ??????????????????.");
            throw new IOException();
        }
        ArrayList<String> namephone= new ArrayList<>();
        this.parent = ac[1];
        this.idr = ac[0];

        if (ac[0].indexOf("razd") < 0 ){
            this.phone = ac[4];
            namephone.add(ac[3]);
            namephone.add(ac[4]);
            try{
                this.tabnum = Integer.parseInt(ac[0]);
            } catch(NumberFormatException e) {
                System.err.println(ac[0] + ". Failed get tabnum from string. " + e.getLocalizedMessage());
                logger.severe(ac[0] + ". Failed get tabnum from string.");
            }
            this.name = cleanw(ac[3]);//((new wwwService()).getApidata().getO(namephone));
            this.mobile = ac[5];
            this.avatar = "www-int"+ac[6];
            this.grade = ac[2];

        } else {
            this.hasChild = true;
            this.name = cleanw(ac[2]);

        }
    }

    public Card(String idr, String name, String parent, Long pid, Boolean hasChild, String parentname) {
        this.date = new Date(System.currentTimeMillis());
        this.idr = idr;
        this.name = name;
        this.parentid = pid;
        this.parent = parent;
        this.hasChild = hasChild;
        this.parentname = parentname;
    }
    
    public Card(Map<String, String> cardMap ) {
        this.date = new Date(System.currentTimeMillis());
        this.idr = cardMap.get("idr");
        this.name = cardMap.get("name");
        this.parent = cardMap.get("parent");
        this.hasChild = cardMap.get("hasChild").equals("'true'") || cardMap.get("hasChild").equals("true");
        if (this.idr.substring(0, 4).equalsIgnoreCase("sotr")) {
            try{
                this.tabnum = Integer.parseInt(cardMap.get("tabnum"));
            } catch(NumberFormatException e) {
                System.err.println(cardMap.get("tabnum") + ". Failed get tabnum from string. " + e.getLocalizedMessage());
                logger.severe(cardMap.get("tabnum") + ". Failed get tabnum from string.");
            }
            this.phone = cardMap.get("phone");
            this.email = cardMap.get("email");
            this.mobile = cardMap.get("mobile");
            this.avatar = cardMap.get("avatar");
            this.grade = cardMap.get("grade");
        }
    }

    public Card(){}

    public String compareCard(Card c){
        String ret = "";
        if (this.phone != null && !this.phone.equals(c.phone)) { ret += "was change internal phone;";}
        if (this.mobile != null && !this.mobile.equals(c.mobile)) { ret += "was change mobile phone;";}
        //if (this.tabnum != c.tabnum) { ret += "was change tabnum;";}
        if (this.avatar != null) {
            if (("error: " + this.avatar).equals(c.avatar)) { //???????? ???? ????????????????????, ???? ???? ???????? ??????????????????
                ret += "reload foto;";
            } else if (!this.avatar.equals(c.avatar)) { //???????? ????????????????????
                ret += "was change avatar image;";
            } 
        }
        if (this.grade != null && !this.grade.toUpperCase().equals(c.grade.toUpperCase())) { ret += "was change grade;";}
        if (!this.name.split(" ")[0].equals(c.name.split(" ")[0])) { ret += "was change name;";}
        return ret;
    }

//    public ArrayList<String> getNamePhone() {
//        ArrayList<String> a = new ArrayList<>();
//        a.add(this.name);
//        a.add(this.phone);
//        return a;
//    }

    public Boolean isParent(){
        if (this.hasChild == null) {
            return false;
        } else {
            return this.hasChild;
        }
    }
    
    public Date getDate(){
        return this.date;
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

    public void setname(String newname){ // ?????????????????? ????????????????
        this.name = newname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public Integer getTabnum() {
        return tabnum;
    }
    public void setTabnum(Integer tab){
        this.tabnum = tab;
    }

    public String getparent() {
        return this.parent;
    }
    public Long getparentid() {
        return this.parentid;
    }
    public void setparentid(Long pid){
        this.parentid = pid;
    }

    public String getparentname() {
        return this.parentname;
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
            ", e-mail='" + email + '\'' +
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
            email + delimiter +
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
        map.put("email",email);
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

    public void setparent(String par) {
        this.parent = par;
    }
}
