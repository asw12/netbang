package ucbang.core;

import java.util.ArrayList;

import ucbang.network.Server;

public class Bang {
    Server server;

    public Player[] players;
    public int numPlayers;
    public int turn;
        public int offturn = -1;
    public Deck deck;
    ArrayList<Card> store;
        int storeIndex;

    public int sheriff;

    public Bang(int p, Server s) {
        server = s;
        numPlayers = p;
        turn = -2;
    }

    /**
     * Helper method to remove some conditions from server and put them here,
     * where it makes more sense
     */
    public void process() {
        int who = turn % numPlayers;
        if (turn == -2) {
            start();
            turn++;
        } else if (turn == -1) {
            start2();
        } else {
            if(offturn>-1){
                if (server.choice.size() == 0) {
                    server.prompt(offturn, "PlayCard", true);
                }
                else if (server.choice.size() >= 1) {
                    offturn = -1;
                }
            }
            
            if (server.choice.size() >= 1) {
                if (server.choice.size() == 1) {
                    System.out.println("You played " + 
                                       server.choice.get(0)[0][1] + 
                                       ". You have " + 
                                       (players[server.choice.get(0)[0][0]].hand.size() - 
                                        1) + " cards left in your hand.");
                }
                if (server.choice.get(0)[0][1] != -1) {
                    if (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).target == 
                        2 && 
                        (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).type == 
                         3? 
                         getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).location ==
                          1:true)) {
                        if (server.choice.size() == 1) {
                            {
                                if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                    server.prompt(turn % numPlayers, "PickCardTarget", true);
                                }
                                else{   
                                    server.prompt(turn % numPlayers, "PickTarget", true);
                                }
                                server.sendInfo(turn%numPlayers, "InfoMsg:Pick Target:0");    
                                return;
                            }
                        } else if (server.choice.size() == 2) {
                            if (server.choice.get(1)[0][1] == -1) {
                                System.out.println("Cancelled");
                                server.choice.remove(server.choice.size() - 1);
                            } else if (isCardLegal(getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]), players[server.choice.get(0)[0][0]], players[server.choice.get(1)[0][1]])) {
                                if (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).discardToPlay == true) {
                                    if (players[server.choice.get(0)[0][0]].hand.size() > 1) {
                                        if (server.choice.get(1).length == 1) { //has not been asked to discard yet
                                            System.out.println("THIS CARD REQUIRES A DISCARD TO PLAY AS WELL");
                                            server.choice.set(1, new int[][] { server.choice.get(1)[0], { server.choice.get(0)[0][0], -2 } }); //TODO: obsolete?
                                            server.prompt(server.choice.get(0)[0][0], 
                                                          "PlayCard", 
                                                          false); //TODO: play card from hand only
                                            System.out.println(server.choice.size() + 
                                                               " " + 
                                                               server.choice.get(1).length);
                                            return;
                                        }
                                        if (server.choice.get(1).length ==  2) { //has  been asked to discard
                                            if (server.choice.get(1)[1][1] == 
                                                server.choice.get(0)[0][1]) {
                                                System.out.println("Cannot discard the card you are playing");
                                                server.choice.set(1, 
                                                                  new int[][] { server.choice.get(1)[0], 
                                                                                { server.choice.get(0)[0][0], 
                                                                                  -2 } });
                                                server.prompt(server.choice.get(0)[0][0], 
                                                              "PlayCard", 
                                                              false); //TODO: play card from hand only
                                                return;
                                            } else {
                                                System.out.println("DISCARD OK.");
                                                //may need to reorder the cards;
                                                if (server.choice.get(0)[0][1] > 
                                                    server.choice.get(1)[1][1]) {
                                                    server.choice.get(0)[0][1]--;
                                                }
                                                playerDiscardCard(server.choice.get(0)[0][0], 
                                                                  server.choice.get(1)[1][1], true);
                                            }
                                        }
                                    } else {
                                        server.choice.remove(server.choice.size() - 
                                                             1);
                                        server.prompt(turn % numPlayers, 
                                                      "PlayCardUnforced", 
                                                      true);
                                        System.out.println("Cannot play that card: you need another card to discard");
                                        return;
                                    }
                                }
                                System.out.println("Player " + 
                                                   server.choice.get(1)[0][0] + 
                                                   " is targeting " + 
                                                   server.choice.get(1)[0][1]);
                                if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                    getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                        server.prompt(turn % numPlayers, 
                                                      "PickCardTarget", 
                                                      true);
                                        return;
                                }
                                server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(0)[0][0] + ":" + getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).name + ":" + server.choice.get(1)[0][1]);
                                if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.JAIL.ordinal()){
                                    server.sendInfo("SetInfo:PutInField:"+server.choice.get(1)[0][1]+":"+
                                                getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).ordinal);
                                    players[server.choice.get(1)[0][1]].field.add(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1])); //give player the jail
                                    playerDiscardCard(server.choice.get(0)[0][0], 
                                                       server.choice.get(0)[0][1], false);
                                    server.choice.remove(server.choice.size()-1);
                                    server.choice.remove(server.choice.size()-1);
                                    server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                                    return;
                                } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DAMAGE.ordinal() ||
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DUEL.ordinal() ) {
                                    if(players[turn%numPlayers].bangs>0 && getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 1 &&
                                     (playerHasFieldEffect(turn % numPlayers, Card.field.GUN)==-1||players[turn % numPlayers].field.get(playerHasFieldEffect(turn % numPlayers, Card.field.GUN)).special!=1)){
                                        server.choice.remove(server.choice.size() - 1);
                                        server.sendInfo(turn%numPlayers, "InfoMsg:Stop banging!:1");    
                                    }                                    
                                    else if(playerHasFieldEffect(server.choice.get(1)[0][1], Card.field.BARREL)>-1&&Math.random()<.25 ||
                                     (players[server.choice.get(1)[0][1]].character==Deck.Characters.JOURDONNAIS.ordinal()?Math.random()<.25:false)){
                                        server.sendInfo("InfoMsg:Player "+server.choice.get(1)[0][1]+" did a barrel roll!:0");
                                        server.choice.remove(server.choice.size() - 1);
                                        playerDiscardCard(server.choice.get(0)[0][0], 
                                                          server.choice.get(0)[0][1], true);
                                    }
                                    else{
                                        System.out.println(players[turn%numPlayers].bangs+"~!@#!@#!@#!@#@!#!#!@#!@#!#@!@#!@#!#!#!#!@#!#!@#12");
                                        server.prompt(server.choice.get(1)[0][1], 
                                                      "PlayCardUnforced", 
                                                      true); //unforce b/c do not have to miss
                                        return;
                                    }
                                } else {
                                    System.out.println("NOT A BANG, SO TARGET DOES NOT CHOOSE (Unimplemented targetting card)");
                                    playerDiscardCard(server.choice.get(0)[0][0], 
                                                      server.choice.get(0)[0][1], true);
                                }
                            }
                            else{
                                server.choice.remove(server.choice.size() - 1);
                            }
                        } else if (server.choice.size() >= 3) {
                            if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                Card.play.DAMAGE.ordinal() && 
                                getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special != 
                                2) {
                                if (server.choice.get(2)[0][1] == -1) { //no miss played
                                    server.sendInfo("SetInfo:CardPlayed:" + 
                                                    server.choice.get(1)[0][1] + 
                                                    ":no miss");
                                    changeLifePoints(server.choice.get(1)[0][1], 
                                                     -1);
                                    if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect2 == 
                                        Card.play.DRAW.ordinal()) {
                                        playerDrawCard(server.choice.get(1)[0][0], 
                                                       1);
                                    }
                                    if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 1 && 
                                     players[turn % numPlayers].character!=Deck.Characters.WILLY_THE_KID.ordinal())//Willy the Kid
                                        players[turn%numPlayers].bangs++;
                                    server.choice.remove(server.choice.size() - 
                                                         1);
                                    server.choice.remove(server.choice.size() - 
                                                         1);
                                    playerDiscardCard(server.choice.get(0)[0][0], 
                                                      server.choice.get(0)[0][1], true);
                                } else if (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).effect == Card.play.MISS.ordinal() && 
                                    (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).type == 3?getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).location>0:true)) {
                                    server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(1)[0][1] + ":" + getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).name);
                                    if (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).effect2 == 
                                        Card.play.DRAW.ordinal()) {
                                        playerDrawCard(server.choice.get(1)[0][1], 
                                                       1);
                                    }
                                    if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect2 == 
                                        Card.play.DRAW.ordinal()) {
                                        playerDrawCard(server.choice.get(1)[0][0], 1);
                                    }
                                    if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 1 &&
                                     players[turn % numPlayers].character!=Deck.Characters.WILLY_THE_KID.ordinal())
                                        players[turn%numPlayers].bangs++;
                                    playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                    playerDiscardCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1], true);
                                    server.choice.remove(server.choice.size() - 1);
                                    server.choice.remove(server.choice.size() - 1);
                                } else { //not a miss card!
                                    System.out.println("May only play a miss!");
                                    server.choice.remove(server.choice.size() - 1);
                                    server.prompt(server.choice.get(1)[0][1], "PlayCardUnforced", true);
                                    return;
                                }
                            }
                            else if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DUEL.ordinal()){
                                if(server.choice.get(server.choice.size()-1)[0][1]==-1){
                                    changeLifePoints(server.choice.get(server.choice.size()-1)[0][0], -1);
                                    playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                    server.choice.remove(server.choice.size() - 1);
                                    server.choice.remove(server.choice.size() - 1);
                                    if(server.choice.size()==4){
                                        server.choice.remove(server.choice.size() - 1);
                                    }
                                }
                                else if(getCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1]).effect == Card.play.DAMAGE.ordinal() && getCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1]).special==1){
                                    if(server.choice.size() == 4){
                                        playerDiscardCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1], true);
                                        if(server.choice.get(server.choice.size()-1)[0][1]<server.choice.get(0)[0][1])
                                            server.choice.get(0)[0][1]--;
                                        server.choice.remove(server.choice.size() - 1);
                                        server.choice.remove(server.choice.size() - 1);
                                        server.prompt(server.choice.get(1)[0][1], "PlayCardUnforced", true);
                                        return;
                                    }
                                    else if(server.choice.size()==3){
                                        playerDiscardCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1], true);
                                        server.prompt(server.choice.get(1)[0][0], "PlayCardUnforced", true);
                                        return;
                                    }
                                    else{
                                        System.out.println("AWEJFAWKFJ@K~$!@#%!!@#RFQFAWEFAWEFA@@#%!&$*#%*#@!$@#%!@#$#YTRQRGDDDZVCX");
                                        return;
                                    }
                                } else{
                                    System.out.println("ILLEGAL CARD");
                                    int i = server.choice.get(server.choice.size() - 1)[0][0];
                                    server.choice.remove(server.choice.size() - 1);
                                    server.prompt(i, "PlayCardUnforced", true);
                                }
                            }
                            else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                    if(server.choice.get(1)[0][1] == server.choice.get(1)[0][0]){
                                        server.choice.remove(server.choice.size() - 1);
                                        server.choice.remove(server.choice.size() - 1);
                                        server.prompt(turn % numPlayers, "PickCardTarget", true);
                                        System.out.println("Cannot panic yourself");
                                        return;
                                    }
                                    server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(0)[0][0] + ":" + getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).name + ":" + server.choice.get(1)[0][1]);
                                    int temp = server.choice.get(2)[0][1];
                                    if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                        if(temp>-1){
                                            playerDiscardCard(server.choice.get(1)[0][1], temp, true);
                                             }
                                        else{
                                            temp = (-temp)-3;
                                            System.out.println("LOOKING FOR A CARD IN FIELD");
                                            playerFieldDiscardCard(server.choice.get(1)[0][1], temp, true);
                                        }
                                    }
                                    else{
                                        if(temp>-1){///askdfjlasjflajfa;lsjkafsf REPLACE THIS WITH GETCARD
                                            players[turn%numPlayers].hand.add(players[(server.choice.get(1)[0][1])].hand.get(temp));
                                            server.sendInfo(turn%numPlayers,"Draw:" + turn%numPlayers + ":Game:"+players[(server.choice.get(1)[0][1])].hand.get(temp).name);
                                            playerDiscardCard(server.choice.get(1)[0][1], temp, false);
                                        }
                                        else{
                                            temp = (-temp)-3;
                                            players[turn%numPlayers].hand.add(players[(server.choice.get(1)[0][1])].field.get(temp));
                                            server.sendInfo(turn%numPlayers,"Draw:" + turn%numPlayers + ":Game:"+players[(server.choice.get(1)[0][1])].field.get(temp).name);
                                            playerFieldDiscardCard(server.choice.get(1)[0][1], temp, false);
                                        }
                                    }
                                    server.choice.remove(server.choice.size() - 1);
                                    server.choice.remove(server.choice.size() - 1);
                                    playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                    //server.prompt(turn%numPlayers, "PlayCardUnforced", true);
                                    //return;
                            }
                        }                        
                    } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).target == 
                               3 &&
                               (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 
                                3? 
                                getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location ==
                                 1:true)) {
                        if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.HEAL.ordinal()) {
                            for (int n = 0; n < numPlayers; n++) {
                                changeLifePoints(n, 1);
                            }
                            playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                        } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                   Card.play.DRAW.ordinal()) {
                                                if(store==null){
                                                    store = new ArrayList<Card>();
                                                    for(int n = 0; n < numPlayers; n++){ //only check living players
                                                        if(players[n].lifePoints>0)
                                                            store.add(drawCard());
                                                    }
                                                    storeIndex = turn % numPlayers;
                                                    
                                                    String s = "";
                                                    for(Card c:store)
                                                        s += ":"+c.name;
                                                    server.sendInfo("SetInfo:GeneralStore:-1"+s);
                                                    server.prompt(storeIndex, "GeneralStore", true);
                                                    return;
                                                }
                                                else{
                                                        Card card = store.remove(server.choice.get(1)[0][1]);
                                                        server.sendInfo(storeIndex, "Draw:"+storeIndex+":Game:" + card.name);
                                                        for(int m = 0; m != storeIndex; m++)
                                                            server.sendInfo(m, "Draw:" + storeIndex + ":" + 1);
                                                        players[storeIndex].hand.add(card);
                                                        
                                                        server.choice.remove(server.choice.size()-1);
                                                        
                                                        storeIndex++;
                                                        if(storeIndex == numPlayers)
                                                            storeIndex = 0;
                                                        
                                                    if(store.size()>0){
                                                        String s = "";
                                                        for(Card c:store)
                                                            s += ":"+c.name;
                                                        server.sendInfo("SetInfo:GeneralStore:-1"+s);
                                                        server.prompt(storeIndex, "GeneralStore", true);
                                                        return;
                                                    }
                                                    else{
                                                        for(int n = 0; n<numPlayers; n++){
                                                            server.sendInfo(n, "SetInfo:GeneralStore:-1");
                                                        }
                                                        store = null;
                                                    }
                                                }
                        } else {                        
                            server.sendInfo("SetInfo:PutInField:"+server.choice.get(0)[0][0]+":"+
                                    getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name+":"+
                                    server.choice.get(0)[0][1]);
                        }

                    } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).target == 
                               4 && 
                                (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 
                                 3? 
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location ==
                                  1:true)) {
                        if (server.choice.size() == 1) {
                            int[] p = new int[numPlayers - 1];
                            for (int n = 0, m = 0; n < numPlayers - 1; n++, m++) {
                                if (m == turn % numPlayers) {
                                    m++;
                                }
                                p[n] = m;
                            }
                            server.promptPlayers(p, "PlayCardUnforced"); //this is for damage cards only!!! need to implement BRAWL here.
                            return;
                        } else if (server.choice.size() == 2) {
                            ArrayList<Integer> al = new ArrayList<Integer>();
                            for (int[] n: server.choice.get(1)) {
                                if (n[1] == -1) {
                                    changeLifePoints(n[0], -1);
                                }
                                else if ((getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 0 && 
                                           getCard(n[0], n[1]).effect == Card.play.MISS.ordinal() && 
                                            getCard(n[0], n[1]).type != 3) || 
                                         (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 2 && 
                                           getCard(n[0], n[1]).effect == Card.play.DAMAGE.ordinal() && 
                                            getCard(n[0], n[1]).special == 1)) {
                                    if (getCard(n[0], n[1]).effect2 == 
                                        Card.play.DRAW.ordinal()) {
                                        playerDrawCard(n[0], 1);
                                    }
                                    server.sendInfo("SetInfo:CardPlayed:" + n[0] + ":" + getCard(n[0], n[1]).name + ":" + n[1]);
                                    playerDiscardCard(n[0], n[1], true);
                                } else {
                                    System.out.println("SOMEONE DID NOT PLAY A LEGAL CARD");
                                    al.add(n[0]);
                                }
                            }
                            if (al.size() == 0){
                                playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                server.choice.remove(server.choice.size() - 1);
                            }
                            else {
                                int[] pp = new int[al.size()];
                                for (int n = 0; n < pp.length; n++) {
                                    pp[n] = al.get(n);
                                }
                                server.choice.remove(server.choice.size() - 1);
                                server.promptPlayers(pp, "PlayCardUnforced");
                                return;
                            }
                        }
                    } 
                    else if((getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 3 || getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 5) && getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location == 0){
                        isCardLegal(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]), players[server.choice.get(0)[0][0]], null);
                        server.sendInfo("SetInfo:PutInField:"+server.choice.get(0)[0][0]+":"+
                                    getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name+":"+
                                    server.choice.get(0)[0][1]);
                        if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name=="DYNAMITE"){
                            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                             getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect2 = turn%numPlayers;
                        }
                        getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location = 2;
                        players[server.choice.get(0)[0][0]].field.add(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]));
                        players[server.choice.get(0)[0][0]].hand.remove(server.choice.get(0)[0][1]);
                        //server.choice.remove(server.choice.size() - 1);
                        //server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                    } else { //self targetting
                        if (isCardLegal(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]), 
                                        players[server.choice.get(0)[0][0]], 
                                        null)) {
                            server.sendInfo("SetInfo:CardPlayed:" + 
                                            server.choice.get(0)[0][0] + ":" + 
                                            getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name);
                            if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                Card.play.DRAW.ordinal()) {
                                playerDrawCard(server.choice.get(0)[0][0], 
                                               getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).range);
                            }
                            if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                Card.play.HEAL.ordinal()) {
                                changeLifePoints(server.choice.get(0)[0][0], 
                                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).range +
                                                 (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special==1 && isCharacter(server.choice.get(0)[0][0],Deck.Characters.TEQUILA_JOE)?1:0));
                            }
                            playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                        }
                    }
                    server.choice.remove(server.choice.size() - 1);
                    server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                }
                if (server.choice.get(0)[0][1] == -1 || 
                    players[server.choice.get(0)[0][0]].hand.size() <= 
                    0) { //TODO: add check for cards that can be played on field as well
                    server.choice.remove(server.choice.size() - 1);
                    nextTurn();
                }
            }
        }
    }

    /**
     * 
     */
    public boolean isCardLegal(Card c, Player p1, 
                               Player p2) {
        if(c.type == 3 && c.location != 1){
            System.out.println("YOU JUST PLAYED THAT GREEN CARD");
            return false;
        }
        if(c.type == 5 && c.location >= 1){
            System.out.println("YOU CAN'T DO ANYTHING WITH THAT");
            return false;
        }
        if (c.target != 2 && p2 != null) {
            System.out.println("NON-TARGETING CARD HAS TARGET");
            return false;
        } else if (c.target == 2 && p2 == null) {
            System.out.println("TARGETING CARD DOES NOT HAVE TARGET");
            return false;
        }
        //the rules
        if (true) { //no idea why i had a condition here
            if ((c.type == 3 || c.type == 5) && players[server.choice.get(0)[0][0]].hand.contains(c)) {
                if(c.type == 5)
                    if(playerHasFieldEffect(server.choice.get(0)[0][0], Card.field.GUN)>-1)
                        playerFieldDiscardCard(server.choice.get(0)[0][0], playerHasFieldEffect(server.choice.get(0)[0][0], Card.field.GUN), true);
            }
            if (c.type == 2 ||(c.type == 3 && players[server.choice.get(0)[0][0]].field.contains(c))) {
                if (c.effect == Card.play.JAIL.ordinal()) {
                    if(p2.role.ordinal()==0){
                        return false; //cannot jail sheriff
                    } else if(p1==p2){
                        return false; //cannot jail self
                    }
                    for(Card card :p2.field){
                        if(card.effect == Card.play.JAIL.ordinal()) //TODO: this sort of check should be for all field cards
                            return false; //TODO: techinically, this should be allowed, and the old jail should be discarded
                    }
                } else if (c.effect == Card.play.HEAL.ordinal()) {
                    if (c.target == 1 && 
                        p1.lifePoints == p1.maxLifePoints) { //can't beer self with max hp
                        System.out.println("ILLEGAL CARD: you are already at maxhp");
                        return false;
                    } else if (c.target == 2 && 
                               p2.lifePoints == p2.maxLifePoints) {
                        System.out.println("ILLEGAL CARD: target is already at maxhp");
                        return false;
                    }
                } else if(c.effect == Card.play.DAMAGE.ordinal()){ 
                    if(p1 == p2) {
                        System.out.println("ILLEGAL CARD: why would you shoot yourself?");
                        return false;
                    }
                }
            } else if (c.type == 4 && 
                       c.effect == Card.play.MISS.ordinal()) { //can't play miss
                System.out.println("ILLEGAL CARD: can't play miss on turn");
                return false;
            }
            if(c.target==2 && c.range!=-1){
                if(c.effect == Card.play.DAMAGE.ordinal()){ 
                    if(c.range == 0){
                        int gun = (playerHasFieldEffect(p1.id, Card.field.GUN)!=-1?p1.field.get(playerHasFieldEffect(p1.id, Card.field.GUN)).range:1);
                        if(getRangeBetweenPlayers(p1, p2)>gun){
                            //System.out.println("YOU RANGE: "+gun+". Target distance: "+ getRangeBetweenPlayers(p1, p2));
                            server.sendInfo(p1.id, "InfoMsg:Player "+server.choice.get(1)[0][1]+" out of range. "+gun+"/"+ getRangeBetweenPlayers(p1, p2)+":1");
                            return false;
                        }
                    }
                    else if(c.range == 1 && getRangeBetweenPlayers(p1, p2)>1)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the distance between p1 and p2. Includes horses and hideouts.
     * @param Player 1, Player 2
     * @return int of how far they are apart
     */
    public int getRangeBetweenPlayers(Player p1, Player p2) {
        int naturalRange = Math.min((numPlayers-p1.id+p2.id)%numPlayers, (numPlayers-p2.id+p1.id)%numPlayers);//seating order
        int distance = naturalRange;
        distance += playerHasFieldEffect(p1.id, Card.field.HORSE_CHASE)>-1?-1:0+playerHasFieldEffect(p2.id, Card.field.HORSE_RUN)>-1?1:0
            + (isCharacter(p1.id, Deck.Characters.SUZY_LAFAYETTE)?-1:0) + (isCharacter(p2.id, Deck.Characters.PAUL_REGRET)?1:0);
        return distance;
    }
    
    boolean isCharacter(int player, Deck.Characters e){
        if(players[player].character==e.ordinal())
            return true;
        return false;
    }

    /**
     * Create p players.
     * Create a draw pile.
     * Show everyone their roles.
     * Give them a choice between two character cards.
     * Give sheriff the first turn.
     * Draw cards equal to the number of life points.
     * Sheriff gets an additional card.
     */
    public void start() {
        //Assign roles
        ArrayList<Enum> roles = new ArrayList<Enum>();
        players = new Player[numPlayers];
        for (int n = 0; n < numPlayers; n++) {
            players[n] = new Player(n, server.names.get(n));
        }

        switch (numPlayers) {
        case 2: //DEBUG MODE
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            break;
        case 4:
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            break;
        case 5:
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            roles.add(Deck.Role.DEPUTY);
            break;
        case 6:
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            roles.add(Deck.Role.DEPUTY);
            roles.add(Deck.Role.OUTLAW);
            break;
        case 7:
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            roles.add(Deck.Role.DEPUTY);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.DEPUTY);
            break;
        case 8:
            roles.add(Deck.Role.SHERIFF);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            roles.add(Deck.Role.DEPUTY);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.OUTLAW);
            roles.add(Deck.Role.RENEGADE);
            break;
        default:
            System.out.print("Bad number of players!");
            System.exit(0);
            break;
        }
        for (int n = 0; n < numPlayers; n++) {
            int role = 
                roles.remove((int)(Math.random() * roles.size())).ordinal();
            if (role == 0) {
                sheriff = n;
                server.sendInfo("SetInfo:role:" + n + ":" + role);
            } else
                server.sendInfo(n, "SetInfo:role:" + n + ":" + role);
            players[n].role = Deck.Role.values()[Integer.valueOf(role)];
        }

        deck = new Deck();
        deck.fillCharacterCards(numPlayers);

        for (int n = 0; n < numPlayers; n++) {
            playerDrawCard(n, 2);
        }

        server.promptAll("ChooseCharacter");
    }


    /**
     * After characters have been chosen, continue starting the game
     */
    public void start2() {
        for (int n = 0; n < server.choice.get(server.choice.size() - 1).length; n++) {
            players[n].characterCard = players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]);
            players[n].character = players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).ordinal;
            server.sendInfo("SetInfo:character:"+n+":"+ players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).ordinal);
        }
        for (int n = 0; n < server.choice.get(server.choice.size() - 1).length; n++) {
            changeMaxLifePoints(n, players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).special + (n == sheriff ? 1 : 0));
        }

        deck.fillGameCards();

        //draw cards equal to lifepoints
        for (Player p1: players) {
            p1.hand.clear();
            playerDrawCard(p1.id, p1.lifePoints);
        }

        //Give Sheriff the first turn (turn 0)
        for (int n = 0; n < numPlayers; n++) {
            System.out.println("Player " + n + " is a " + players[n].role);
            if (players[n].role == Deck.Role.SHERIFF) {
                turn = n - 1;
                break;
            }
        }
        server.choice.clear();
        nextTurn();
    }

    //returns false if game is over

    public void nextTurn() {
        turn++;

        if (server.choice.size() > 1) {
            System.out.println("ERROR: NEW TURN CALLED TOO EARLY");
            turn--;
            return;
        }

        //check if player is dead
        int oldturn = turn;
        while (players[turn % numPlayers].lifePoints == 0 && 
               turn - oldturn < numPlayers) {
            turn++;
        }

        server.sendInfo("SetInfo:turn:" + turn);
        
        for(int n = 0; n<players[turn % numPlayers].field.size(); n++){
            Card c = players[turn % numPlayers].field.get(n);
            if(c.effect == Card.play.JAIL.ordinal()){
                if(c.type==2){
                    playerFieldDiscardCard(turn%numPlayers, n, true);
                     if(Math.random()<.75){
                        nextTurn();
                        return;
                    }
                } 
                else{ //dynamite
                    if(Math.random()<0){ //BOOM
                        playerFieldDiscardCard(turn%numPlayers, n, true); 
                        System.out.println("ASPLODEDEDEDEDEDEDEDEDEDEDED!11111!!$!@#$!@#$");
                        //something to end the guy's turn if he's dead
                    }
                    else{
                        int a = turn+1;
                        while (players[a  % numPlayers].lifePoints == 0 && 
                               a - turn < numPlayers) {
                            a++;
                        }
                        if(players[turn%numPlayers].field.get(n).effect2 != a%numPlayers){
                            server.sendInfo("SetInfo:PutInField:"+a%numPlayers+":"+
                                        players[turn%numPlayers].field.get(n).ordinal);
                            players[a%numPlayers].field.add(players[turn%numPlayers].field.get(n));
                        }
                        playerFieldDiscardCard(turn%numPlayers, n, true);
                    }
                }
            }
        }
        
        for(Card c : players[turn % numPlayers].field){ //set all green cards played the turn before to 1
            c.location = 1;
        }
        
        players[turn%numPlayers].bangs = 0;
        
        //draw two cards
        if (players[turn%numPlayers].characterCard.effect != 1) { //TODO: get rid of specialDraw, move to a direct reference to character cards
            playerDrawCard(turn % numPlayers, 2);
        } else {
            switch((Deck.Characters)players[turn%numPlayers].characterCard.e){
                case BLACK_JACK: //TODO: reveal second card drawn.
                    playerDrawCard(turn % numPlayers, (Math.random()<.5?3:2));
                    break;
                case JESSE_JONES:
                playerDrawCard(turn % numPlayers, 2);
                    break;
                case KIT_CARLSON:
                playerDrawCard(turn % numPlayers, 2);
                    break;
                case PEDRO_RAMIREZ:
                playerDrawCard(turn % numPlayers, 2);
                    break;
                case BILL_NOFACE:
                playerDrawCard(turn % numPlayers, 2);
                    break;
                case PAT_BRENNAN:
                playerDrawCard(turn % numPlayers, 2);
                    break;
                case PIXIE_PETE:
                    playerDrawCard(turn % numPlayers, 4);
                    break;
                default: 
                    break;
                //
            }
        }
        System.out.println("It is turn " + turn % numPlayers);
        server.prompt(turn % numPlayers, "PlayCardUnforced", true);
    }

    /**
     * If written, this method would replace the return value above
     */
    public boolean isGameWon() {
        int deputies = 0;
        int sheriff = 0;
        int outlaws = 0;
        int renegades = 0;
        for (Player p: players) {
            if (p.lifePoints > 0) {
                switch ((Deck.Role)p.role) {
                case DEPUTY:
                    deputies++;
                case SHERIFF:
                    sheriff++;
                    break;
                case OUTLAW:
                    outlaws++;
                    break;
                case RENEGADE:
                    renegades++;
                    break;
                }
            }
        }
        if (sheriff == 0) {
            if (outlaws == 0 && deputies == 0) {
                System.out.println("Renegades win!");
                return true;
            } else {
                System.out.println("Outlaws win!");
                return true;
            }
        }
        if (sheriff >= 0) {
            if (outlaws == 0 && deputies == 0) {
                System.out.println("Sheiff+Deputies win!");
                return true;
            }
        }
        return false;
    }

    public void playerDrawCard(int p, int n) {
        if (n <= 0)
            return; //must draw at least 1
        Card c = drawCard();
        String s = 
            "Draw:" + p + ":" + (c.type == 1 ? "Character:" : "Game:") + c.name + 
            ":"; //need this to get the card type
        players[p].hand.add(c);
        for (int m = 1; m < n; m++) {
            c = drawCard();
            s = s + c.name + ":";
            players[p].hand.add(c);
        }
        server.sendInfo(p, s);
        if (c.type != 1) {
            for (int m = 0; m < numPlayers; m++) {
                if (m != p) {
                    server.sendInfo(m, "Draw:" + p + ":" + n);
                }
            }
        }
    }
    
    /**
     * Discards Player p's hand
     */
    public void playerDiscardHand(Player p) {
        for (int n = p.hand.size() - 1; n >= 0; n--)
            playerDiscardCard(p.id, n, true);
    }

    /**
     * Discards card n in Player p's hand
     */
    public void playerDiscardCard(int p, int n, boolean dp) {
        if(n<-2){
            playerFieldDiscardCard(p, -n-3, dp);
            return;
        }
        Card c = players[p].hand.get(n);
        //is card a character card
        players[p].hand.remove(c);
        if (c.type == 1) {
            //players[p].hand.remove(n);
        } else {
            server.sendInfo(p, "SetInfo:discard:"+p+":"+n);
            if(dp){
                deck.discardPile.add(c);
                for (int m = 0; m < numPlayers; m++) {
                    if (m != p) {
                        server.sendInfo(m, "SetInfo:discard:"+p+":"+n+":"+ c.name);
                    }
                }
            }
            else{
                for (int m = 0; m < numPlayers; m++) {
                    if (m != p) {
                        server.sendInfo(m, "SetInfo:discard:"+p+":"+n);
                    }
                }
            }
        }
    }
    /**
     * Discards card n in Player p's field
     */
    public void playerFieldDiscardCard(int p, int n, boolean dp) {
        Card c = players[p].field.get(n);
        //is card a character card
        players[p].field.remove(c);
        if (c.type == 1) {
            //players[p].hand.remove(n);
        } else {
            server.sendInfo(p, "SetInfo:fieldDiscard:"+p+":"+n);
            if(dp)
                deck.discardPile.add(c);
            for (int m = 0; m < numPlayers; m++) {
                if (m != p) {
                    server.sendInfo(m, "SetInfo:fieldDiscard:"+p+":"+n+":"+ c.name);
                }
            }
        }
        }
    
    /**
     * Moves a card from p's hand to his field
     */
    public void playerMoveToField(int p, int n) {
        /*Card c = players[p].hand.get(n);
        //is card a character card
        players[p].hand.remove(c);
        server.sendInfo("SetInfo:moveToField:"+p+":"+c.name);
        deck.discardPile.add(c);*/
    }

    /**
     * Flips the top card of the drawPile. This card is then put in the discard 
     * pile. Used for barrels and other effects.
     * @return
     */
    public Card flipCard() {
        Card c = drawCard();
        deck.discardPile.add(drawCard());
        return c;
    }

    /**
     * Draws one card. This card is either returned to the flipCard method or
     * the playerDrawCard method.
     * @param
     * @return Card
     */
    public Card drawCard() {
        if (deck.drawPile.size() == 0) {
            shuffleDeck();
        }
        System.out.println(deck.drawPile.peekLast().name);
        return deck.drawPile.removeLast();
    }

    /**
     * Shuffles the discard pile back into the deck.
     * Only used when draw pile is empty
     */
    public void shuffleDeck() {
        if (deck.drawPile.size() > 0) {
            System.out.println("Error: did not need to shuffleDeck()");
            return;
        }
        while (deck.discardPile.size() > 0) {
            deck.drawPile.add(deck.discardPile.remove((int)Math.random() * 
                                                      deck.discardPile.size()));
        }
    }

    void changeMaxLifePoints(int p, int n) {
        server.sendInfo("SetInfo:maxHP:" + p + ":" + n);
        players[p].maxLifePoints += n;
        players[p].lifePoints = players[p].maxLifePoints;
    }

    void changeLifePoints(int p, int n) {
        if (players[p].lifePoints + n > players[p].maxLifePoints) {
            System.out.println("Player " + p + " is now at max hp.");
            n = players[p].maxLifePoints - players[p].lifePoints;
            server.sendInfo("SetInfo:HP:" + p +":"+ n);
            players[p].lifePoints += n;
        } else {
            server.sendInfo("SetInfo:HP:" + p + ":" + n);
            players[p].lifePoints += n;
        }
    }
    
    Card getCard(int p, int n){
        if(n>-1){
            return players[p].hand.get(n);
        }
        if(n<-2){
            return players[p].field.get(-n-3);
        }
        System.out.println("ERRORERRORERROR12345");
        return null;
    }
    
    /**
     * Returns the index in field of the card in player p's field granting the effect e.
     * If p does not have the effect, returns -1.
     * @param p
     * @param e
     * @return
     */
    public int playerHasFieldEffect(int p, Card.field e){ //TODO: this
        for(int n = 0; n<players[p].field.size(); n++){
            Card c = players[p].field.get(n);
            if(e.ordinal()==c.effect){
                return n;
            }
        }
        return -1;
    }
}
