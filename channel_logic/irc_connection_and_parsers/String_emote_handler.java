package channel_logic.irc_connection_and_parsers;

import java.util.ArrayList;

/**
 * Created by Leander on 24.09.2016.
 *
 * Class used by whisper and string handler to insert the emote flag and codes into the message, which are later used to identify emotes.
 */
class String_emote_handler {

    private String last_checked_string = "";
    private int[] amount = {0,0};

    //Parses the message for the emotes, adds emotecodes to the message
    String parse_emotes(String message, String emotecode)
    {
        if(message.equals(""))return "";
        if(emotecode==null||emotecode.equals(""))return message;
        String[] emotecodes = emotecode.split("/");
        ArrayList<Integer> emote_positions = new ArrayList<>();
        ArrayList<Integer> emotesets = new ArrayList<>();

        for(String s:emotecodes)
        {
            String emotes = s.substring(s.indexOf(":")+1);
            String emotenumber = s.substring(0,s.indexOf(":"));
            int emote_index = Integer.parseInt(emotenumber);

            String[] subemotes = emotes.split(",");
            for(String emote:subemotes)
            {
                int index = Integer.parseInt(emote.split("-")[0]);
                emote_positions.add(index);
                emotesets.add(emote_index);
            }
        }
        return add_emotecodes(emote_positions,emotesets,message);
    }



    //Adds the emotecodes to the message, uses surrogate safe insertion to prevent unicode emotes misplacing the insertion
    private String add_emotecodes(ArrayList<Integer> index_list,ArrayList<Integer> emotenumber,String message)
    {
        dualsort(index_list,emotenumber,0,index_list.size()-1);

        int counter = 0;
        //Detect a colored message
        if(message.length()>7&&message.substring(0,7).matches("\\0001ACTION")){counter+=8;}
        for(int i=0;i<index_list.size();i++)
        {
            message = surrogate_safe_insertion(message,index_list.get(i)+counter,"[EMOTE]("+emotenumber.get(i)+")");
            counter += 7;//length of [EMOTE]
            counter += (emotenumber.get(i)+"").length()+2;//length of (emotenumber)
        }
        return message;
    }



    //Checks for surrogate pairs in Strings and modifies insertion-pointer accordingly
    private String surrogate_safe_insertion(String message,int index, String insertion)
    {
        index += count_surrogates(message,index);
        return message.substring(0,index)+insertion+message.substring(index);
    }

    //counts the low surrogates up unto a certain index, caches result to improve performance for large strings
    private int count_surrogates(String message, int index)
    {
        if(!last_checked_string.equals(message))
        {
            last_checked_string = message;
            amount[0]=0;
            amount[1]=0;
        }
        int sur = amount[1];
        for(int i = amount[0];i<index+sur;i++)
        {
            if(Character.isLowSurrogate(message.charAt(i)))
            {
                ++sur;
            }
        }
        amount[0] = index+sur;
        amount[1] = sur;

        return sur;
    }

    //custom sort function, changes in indices of elements in A are also applied to the same indices of B
    //uses a custom implementation of quicksort
    private void dualsort(ArrayList<Integer> a, ArrayList<Integer> b,int start, int finish)
    {
        if(start>=finish||finish>=a.size())return;
        int pivot = a.get(start);
        int shifts = 0;
        for(int i=start+1;i<=finish;i++)
        {
            if(a.get(i)<pivot)
            {
                dualmove(a,b,i,start);
                ++shifts;
            }
        }
        dualsort(a,b,start,start+shifts);
        dualsort(a,b,start+shifts+1,finish);
    }

    //function solely used in dualsort, moves an element from index "from to index "to" in both input arraylists
    private void dualmove(ArrayList<Integer> a,ArrayList<Integer> b,int from, int to)
    {
        int shiftval = a.get(from);
        a.remove(from);
        a.add(to,shiftval);
        shiftval = b.get(from);
        b.remove(from);
        b.add(to,shiftval);
    }

}
