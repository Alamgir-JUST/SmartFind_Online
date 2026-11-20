package edu.smartfind.service;
import edu.smartfind.model.Item;
import java.time.LocalDate;
import java.util.List;
public class MatchSmokeTest {
    static Item item(long id,String type,String category,String color,String location,String desc,LocalDate date,String status){
        return new Item(id,1,"Test","test@example.edu",type,"Calculator",category,color,location,desc,date,status);
    }
    public static void main(String[] args) {
        var date=LocalDate.of(2026,11,18);
        var lost=item(1,"LOST","Electronics","Black","Library","casio fx991",date,"OPEN");
        var found=item(2,"FOUND","Electronics","Black","Library","casio fx991",date.plusDays(1),"OPEN");
        assertEquals(100,MatchService.score(lost,found));
        assertEquals(0,MatchService.score(lost,item(3,"LOST","Electronics","Black","Library","casio",date,"OPEN")));
        assertEquals(0,MatchService.score(lost,item(4,"FOUND","Electronics","Black","Library","casio",date,"RETURNED")));
        if(MatchService.find(lost,List.of(found)).size()!=1) throw new AssertionError("Expected match");
        System.out.println("PASS: 4 matching smoke checks");
    }
    static void assertEquals(int want,int actual){if(want!=actual) throw new AssertionError("Expected "+want+" got "+actual);}
}
