package edu.smartfind.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.smartfind.dao.ItemDAO;
import edu.smartfind.model.Item;
import edu.smartfind.service.MatchService;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Executors;

/** Four-page Java web app using the standard JDK HTTP server and JDBC (no Spring required). */
public class SmartFindServer {
    private final HttpServer server;
    private final ItemDAO dao=new ItemDAO();
    public SmartFindServer(int port) throws IOException {
        server=HttpServer.create(new InetSocketAddress("0.0.0.0",port),0);
        server.createContext("/",this::dispatch);
        server.setExecutor(Executors.newFixedThreadPool(8));
    }
    public void start(){server.start();}
    public void stop(){server.stop(0);}
    private void dispatch(HttpExchange ex) throws IOException {
        try {
            String path=ex.getRequestURI().getPath();
            String method=ex.getRequestMethod();
            if (path.equals("/assets/style.css")) {css(ex);return;}
            if (path.equals("/favicon.ico")) {send(ex,204,"");return;}
            if(path.equals("/")){redirect(ex,"/report");return;}
            if(path.equals("/report") && method.equals("GET")) {page(ex,"Report item","report",report(null));return;}
            if(path.equals("/report") && method.equals("POST")) {create(ex);return;}
            if(path.equals("/items") && method.equals("GET")) {items(ex);return;}
            if(path.equals("/matches") && method.equals("GET")) {matches(ex);return;}
            if(path.equals("/manage") && method.equals("GET")) {manage(ex);return;}
            if(path.equals("/manage/edit") && method.equals("GET")) {edit(ex);return;}
            if(path.equals("/manage/update") && method.equals("POST")) {update(ex);return;}
            if(path.equals("/manage/delete") && method.equals("POST")) {delete(ex);return;}
            pageWithStatus(ex,404,"Not found","", "<section class='panel'><h2>404 · Page not found</h2><a href='/report'>Return to report</a></section>");
        }catch(IllegalArgumentException e){pageWithStatus(ex,400,"Invalid input","",message("Please check the submitted information."));}
         catch(SQLException e){System.err.println("Database error: "+e.getMessage());pageWithStatus(ex,503,"Database unavailable","",message("Cannot connect to the database. Review the Codespaces terminal and database configuration."));}
         catch(Exception e){e.printStackTrace();pageWithStatus(ex,500,"Server error","",message("An unexpected error occurred. Review the terminal."));}
        finally{ex.close();}
    }
    private void create(HttpExchange ex) throws Exception {
        Map<String,String> p=post(ex);
        dao.insert(required(p,"owner",100),email(p),choice(p,"type","LOST","FOUND"),required(p,"name",100),
            required(p,"category",50),required(p,"color",40),required(p,"location",100),required(p,"description",500),date(p));
        redirect(ex,"/items?message=created");
    }
    private void items(HttpExchange ex) throws Exception {
        Map<String,String> q=query(ex);String search=q.getOrDefault("q","").trim();if(search.length()>100)search=search.substring(0,100);
        String type=q.getOrDefault("type","");if(!type.equals("LOST")&&!type.equals("FOUND"))type="";
        List<Item> all=dao.all(search,type);
        StringBuilder b=new StringBuilder("<div class='heading'><div><span class='eyebrow'>Page 02 / READ</span><h1>Explore reported items</h1><p>Search campus reports and view the matching details.</p></div><span class='pill'>"+all.size()+" records</span></div>");
        b.append("<form class='filters' method='get'><input type='search' name='q' placeholder='Search item, owner or location...' value='").append(h(search)).append("'><select name='type'><option value=''>All types</option>")
         .append(option("LOST",type,"Lost")).append(option("FOUND",type,"Found")).append("</select><button class='btn'>Search</button></form>");
        if(q.containsKey("message"))b.append("<div class='notice'>Item successfully created.</div>");
        b.append("<section class='cards'>");for(Item i:all)b.append(itemCard(i));b.append("</section>");
        if(all.isEmpty())b.append("<div class='empty'>No items found. Try another search or <a href='/report'>report an item</a>.</div>");
        page(ex,"Explore Items","items",b.toString());
    }
    private void matches(HttpExchange ex) throws Exception {
        Map<String,String> q=query(ex);List<Item> all=dao.all("", "");
        StringBuilder b=new StringBuilder("<div class='heading'><div><span class='eyebrow'>Page 03 / MATCHING</span><h1>Smart match explorer</h1><p>Compare open LOST reports with open FOUND reports using an explainable score.</p></div></div>");
        b.append("<form method='get' class='filters'><select name='id' required><option value=''>Choose a reported item...</option>");
        long chosen=positiveIdOrZero(q.get("id"));for(Item i:all)if(i.status().equals("OPEN")) {
            b.append("<option value='").append(i.id()).append("' ").append(i.id()==chosen?"selected":" ").append(">").append(h("#"+i.id()+" "+i.type()+" · "+i.name())).append("</option>");
        }
        b.append("</select><button class='btn'>Find matches</button></form>");
        if(chosen>0){Item source=all.stream().filter(i->i.id()==chosen).findFirst().orElse(null);
            if(source==null){b.append(message("Item not found."));}else if(!source.status().equals("OPEN")){b.append(message("Only OPEN items can be matched."));}
            else {b.append("<div class='section-title'><h2>Potential matches for ").append(h(source.name())).append("</h2><span class='pill'>").append(h(source.type())).append("</span></div>");
                List<MatchService.Match> ms=MatchService.find(source,all);
                if(ms.isEmpty())b.append("<div class='empty'>No candidate matches currently available.</div>");
                for(var m:ms){b.append("<article class='match panel'><div class='match-head'><div><strong>").append(h(m.item().name())).append("</strong><small>").append(h(m.item().type()+" · "+m.item().location())).append("</small></div><div class='score'>").append(m.score()).append(" / 100</div></div><div class='bar'><div style='width:").append(m.score()).append("%'></div></div><p>").append(h(m.item().description())).append("</p><p class='muted'>Reported ").append(h(m.item().date().toString())).append(" · Contact: ").append(h(m.item().ownerEmail())).append("</p></article>");}
            }
        }
        b.append("<div class='hint'><strong>How is the score calculated?</strong> Same category 30, color 20, location 20, shared description/name word 20, report dates within 7 days 10 (or within 30 days 5). Only opposite types and OPEN reports qualify. This is rule-based matching, not machine learning. A score is not proof of ownership.</div>");
        page(ex,"Smart Matching","matches",b.toString());
    }
    private void manage(HttpExchange ex) throws Exception {
        List<Item> all=dao.all("","");StringBuilder b=new StringBuilder("<div class='heading'><div><span class='eyebrow'>Page 04 / UPDATE + DELETE</span><h1>Manage reports</h1><p>Edit details, mark items returned, or remove test records.</p></div></div>");
        if(query(ex).containsKey("saved"))b.append("<div class='notice'>Changes saved.</div>");
        b.append("<div class='table-wrap panel'><table><thead><tr><th>ID</th><th>Item</th><th>Type</th><th>Student</th><th>Status</th><th>Actions</th></tr></thead><tbody>");
        for(Item i:all){b.append("<tr><td>#").append(i.id()).append("</td><td><strong>").append(h(i.name())).append("</strong><small>").append(h(i.location())).append("</small></td><td>").append(h(i.type())).append("</td><td>").append(h(i.ownerName())).append("</td><td><span class='tag'>").append(h(i.status())).append("</span></td><td><div class='actions'><a class='btn ghost small' href='/manage/edit?id=").append(i.id()).append("'>Edit</a><form method='post' action='/manage/delete' onsubmit=\"return confirm('Delete this item permanently?')\"><input type='hidden' name='id' value='").append(i.id()).append("'><button class='btn danger small' type='submit'>Delete</button></form></div></td></tr>");}
        b.append("</tbody></table></div>");page(ex,"Manage Items","manage",b.toString());
    }
    private void edit(HttpExchange ex) throws Exception {
        Item i=dao.byId(positiveId(query(ex).get("id")));
        if(i==null){pageWithStatus(ex,404,"Missing item","manage",message("The requested item does not exist."));return;}
        StringBuilder b=new StringBuilder("<div class='heading'><div><span class='eyebrow'>Page 04 / UPDATE</span><h1>Edit item #"+i.id()+"</h1><p>Owner: "+h(i.ownerName())+" ("+h(i.ownerEmail())+")</p></div></div>");
        b.append("<form class='panel edit-form' method='post' action='/manage/update'><input type='hidden' name='id' value='").append(i.id()).append("'>");
        b.append(fields(i));b.append("<label>Status<select name='status'>").append(option("OPEN",i.status(),"Open")).append(option("RETURNED",i.status(),"Returned")).append("</select></label>");
        b.append("<div class='form-actions'><button class='btn' type='submit'>Save changes</button><a class='btn ghost' href='/manage'>Cancel</a></div></form>");page(ex,"Edit Item","manage",b.toString());
    }
    private void update(HttpExchange ex) throws Exception {
        Map<String,String> p=post(ex);boolean done=dao.update(positiveId(p.get("id")),choice(p,"type","LOST","FOUND"),required(p,"name",100),required(p,"category",50),required(p,"color",40),required(p,"location",100),required(p,"description",500),date(p),choice(p,"status","OPEN","RETURNED"));
        if(!done){pageWithStatus(ex,404,"Missing item","manage",message("No record updated."));return;}redirect(ex,"/manage?saved=1");
    }
    private void delete(HttpExchange ex) throws Exception {dao.delete(positiveId(post(ex).get("id")));redirect(ex,"/manage?saved=1");}
    private String report(Item i) {
        return "<div class='heading'><div><span class='eyebrow'>Page 01 / CREATE</span><h1>Every lost item deserves a way home.</h1><p>Report an item and help your campus reconnect students with belongings.</p></div><div class='hero-icon'>✦</div></div>"+
          "<div class='split'><form class='panel report-form' method='post' action='/report'><h2>New item report</h2><div class='form-grid'><label>Your full name<input name='owner' maxlength='100' required placeholder='e.g. Ayesha Rahman'></label><label>Student email<input type='email' name='email' maxlength='150' required placeholder='name@example.edu'></label>"+
          fields(i)+"</div><div class='form-actions'><button class='btn' type='submit'>Submit report</button><span class='muted'>Saved to MySQL using JDBC</span></div></form>"+
          "<aside class='side-card'><span class='eyebrow'>SMARTFIND AT A GLANCE</span><h2>Small project. Real impact.</h2><p>Java OOP meets a relational database in a campus-friendly four-page application.</p><div class='feature'>◉ &nbsp; Create / Read / Update / Delete</div><div class='feature'>◉ &nbsp; Prepared SQL statements</div><div class='feature'>◉ &nbsp; Explainable match scores</div><div class='feature'>◉ &nbsp; Live MySQL data</div><p class='muted'>Demo only: use fictional student contact details.</p></aside></div>";
    }
    private String fields(Item i) {
        String type=i==null?"LOST":i.type(),name=i==null?"":i.name(),category=i==null?"":i.category(),color=i==null?"":i.color(),location=i==null?"":i.location(),desc=i==null?"":i.description();
        String date=i==null?LocalDate.now().toString():i.date().toString();
        return "<label>Report type<select name='type'>"+option("LOST",type,"Lost")+option("FOUND",type,"Found")+"</select></label>"+
            "<label>Item name<input name='name' maxlength='100' required value='"+h(name)+"' placeholder='e.g. Scientific calculator'></label>"+
            "<label>Category<select name='category'>"+categories(category)+"</select></label>"+
            "<label>Color<input name='color' maxlength='40' required value='"+h(color)+"' placeholder='e.g. Black'></label>"+
            "<label>Campus location<input name='location' maxlength='100' required value='"+h(location)+"' placeholder='e.g. Library'></label>"+
            "<label>Lost / found date<input type='date' name='date' value='"+h(date)+"' required></label>"+
            "<label class='full'>Description<textarea name='description' maxlength='500' rows='4' required placeholder='Model, identifying features, where you saw it...'>"+h(desc)+"</textarea></label>";
    }
    private String categories(String chosen){StringBuilder b=new StringBuilder();for(String c:new String[]{"Electronics","ID Card","Books","Accessories","Clothing","Other"})b.append(option(c,chosen,c));return b.toString();}
    private String itemCard(Item i){return "<article class='item-card'><div class='item-icon'>"+(i.category().equals("Electronics")?"⌘":i.category().equals("Books")?"▤":"✦")+"</div><div class='card-top'><span class='tag "+i.type().toLowerCase()+"'>"+h(i.type())+"</span><span class='muted'>#"+i.id()+"</span></div><h3>"+h(i.name())+"</h3><p>"+h(i.description())+"</p><div class='meta'>"+h(i.category())+" · "+h(i.color())+" · "+h(i.location())+"</div><div class='card-bottom'><span>"+h(i.date().toString())+" · "+h(i.status())+"</span><a href='/matches?id="+i.id()+"'>Find matches →</a></div></article>";}
    private void page(HttpExchange ex,String title,String active,String body) throws IOException {pageWithStatus(ex,200,title,active,body);}
    private void pageWithStatus(HttpExchange ex,int status,String title,String active,String body) throws IOException {
        String html="<!doctype html><html lang='en'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1'><title>"+h(title)+" · SmartFind</title><link rel='stylesheet' href='/assets/style.css'></head><body>"+
          "<div class='shell'><aside class='sidebar'><div class='brand'><div class='logo'>S<span>✦</span></div><div><strong>SmartFind</strong><small>CAMPUS CONNECT</small></div></div><div class='nav-title'>WORKSPACE</div>"+
          nav("/report","01","Report Item","report",active)+nav("/items","02","Explore Items","items",active)+nav("/matches","03","Smart Matching","matches",active)+nav("/manage","04","Manage Items","manage",active)+
          "<div class='sidebar-footer'><span class='online-dot'></span> Java + MySQL<br><small>OOP · JDBC · CRUD</small></div></aside><main class='main'><header class='top'><span>Student DBMS Lab / Java Web Edition</span><div class='top-right'>✳ <span>Classroom Demo</span></div></header><div class='content'>"+body+"</div><footer>Built for DBMS lab learning · No real personal data · Rule-based matching</footer></main></div></body></html>";
        byte[] bytes=html.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type","text/html; charset=utf-8");ex.getResponseHeaders().set("X-Content-Type-Options","nosniff");ex.sendResponseHeaders(status,bytes.length);try(OutputStream os=ex.getResponseBody()){os.write(bytes);}
    }
    private String nav(String path,String number,String label,String key,String active){return "<a class='nav "+(key.equals(active)?"active":"")+"' href='"+path+"'><span class='nav-num'>"+number+"</span>"+label+"</a>";}
    private String message(String s){return "<section class='panel'><p>"+h(s)+"</p><a href='/report'>Go to report page</a></section>";}
    private String option(String value,String selected,String label){return "<option value='"+h(value)+"' "+(value.equals(selected)?"selected":"")+">"+h(label)+"</option>";}
    private static String h(String s){if(s==null)return "";return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
    private Map<String,String> query(HttpExchange ex){return parse(ex.getRequestURI().getRawQuery());}
    private Map<String,String> post(HttpExchange ex) throws IOException {
        String contentType=ex.getRequestHeaders().getFirst("Content-Type");if(contentType==null||!contentType.startsWith("application/x-www-form-urlencoded"))throw new IllegalArgumentException("Unsupported form type");
        byte[] data=ex.getRequestBody().readNBytes(16000);if(data.length==16000)throw new IllegalArgumentException("Form too large");return parse(new String(data,StandardCharsets.UTF_8));
    }
    private Map<String,String> parse(String encoded){Map<String,String> map=new HashMap<>();if(encoded==null||encoded.isEmpty())return map;for(String pair:encoded.split("&")){String[] kv=pair.split("=",2);map.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),URLDecoder.decode(kv.length>1?kv[1]:"",StandardCharsets.UTF_8));}return map;}
    private String required(Map<String,String> p,String name,int max){String v=p.getOrDefault(name,"").trim();if(v.isBlank()||v.length()>max)throw new IllegalArgumentException("Invalid "+name);return v;}
    private String email(Map<String,String> p){String e=required(p,"email",150);if(!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Invalid email");return e;}
    private LocalDate date(Map<String,String> p){LocalDate d=LocalDate.parse(required(p,"date",10));if(d.isAfter(LocalDate.now().plusDays(1)))throw new IllegalArgumentException("Future date");return d;}
    private String choice(Map<String,String> p,String name,String... values){String s=p.getOrDefault(name,"");if(!Arrays.asList(values).contains(s))throw new IllegalArgumentException("Invalid "+name);return s;}
    private long positiveId(String s){long id=Long.parseLong(s);if(id<1)throw new IllegalArgumentException("Invalid ID");return id;}
    private long positiveIdOrZero(String s){try{return positiveId(s);}catch(Exception e){return 0;}}
    private void redirect(HttpExchange ex,String destination) throws IOException {ex.getResponseHeaders().set("Location",destination);ex.sendResponseHeaders(303,-1);}
    private void send(HttpExchange ex,int code,String text) throws IOException{byte[] bytes=text.getBytes(StandardCharsets.UTF_8);ex.sendResponseHeaders(code,code==204?-1:bytes.length);if(code!=204)try(OutputStream os=ex.getResponseBody()){os.write(bytes);}}
    private void css(HttpExchange ex) throws IOException {byte[] bytes;try(InputStream in=SmartFindServer.class.getResourceAsStream("/style.css")){bytes=Objects.requireNonNull(in).readAllBytes();}ex.getResponseHeaders().set("Content-Type","text/css; charset=utf-8");ex.sendResponseHeaders(200,bytes.length);try(OutputStream os=ex.getResponseBody()){os.write(bytes);}}
}
