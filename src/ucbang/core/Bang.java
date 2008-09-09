package ucbang.core;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

import ucbang.gui.ClientGUI;

import ucbang.network.Server;

public class Bang {
    public Bang(int p, Server s) {
        server = s;
        gui = new ClientGUI[p]; //deleteme
        start(p);
    }
    
    /*public static void main(String[] args){
        new Bang();
    }*/
    Server server;
    
    public ClientGUI[] gui;// deleteme
    
    public Player[] players;
    public int numPlayers;
    
    public int turn;
    
    public ArrayList<Card> drawPile = new ArrayList<Card>(); //the card on the bottom in stored in index 0, the card on top is stored in index size()-1
    public ArrayList<Card> discardPile = new ArrayList<Card>();
    
    public static enum CardName {BANG, MISS, BEER, BARREL, DUEL, INDIANS, GATLING, DYNAMITE, SALOON, WELLS_FARGO, STAGECOACH, GENERAL_STORE, CAT_BALLOU, PANIC, JAIL, APPALOOSA, MUSTANG, VOLCANIC, SCHOFIELD, REMINGTON, REV_CARBINE, WINCHESTER, HIDEOUT, SILVER, BRAWL, DODGE, PUNCH, RAG_TIME, SPRINGFIELD, TEQUILA, WHISKY, BIBLE, BUFFALO_RIFLE, CAN_CAN, CANTEEN, CONESTOGA, DERRINGER, HOWITZER, IRON_PLATE, KNIFE, PEPPERBOX, PONY_EXPRESS, SOMBRERO, TEN_GALLON_HAT};
    public static enum Characters {BART_CASSIDY, BLACK_JACK, CALAMITY_JANET, EL_GRINGO, JESSE_JONES, JOURDONNAIS, KIT_CARLSON, LUCKY_DUKE, PAUL_REGRET, PEDRO_RAMIREZ, ROSE_DOOLAN, SID_KETCHUM, SLAB_THE_KILLER, SUZY_LAFAYETTE, VULTURE_SAM, WILLY_THE_KID, APACHE_KID, BELLE_STAR, BILL_NOFACE, CHUCK_WENGAM, DOC_HOLYDAY, ELENA_FUENTE, GREG_DIGGER, HERB_HUNTER, JOSE_DELGADO, MOLLY_STARK, PAT_BRENNAN, PIXIE_PETE, SEAN_MALLORY, TEQUILA_JOE, VERA_CUSTER};
    public static enum Role {SHERIFF, DEPUTY, OUTLAW, RENEGADE};
    
    //public ClientGUI[] gui;
    
    /**
     * Create p players.
     * Create a draw pile.
     * Show everyone their roles.
     * Give them a choice between two character cards.
     * Give sheriff the first turn.
     * Draw cards equal to the number of life points.
     * Sheriff gets an additional card.
     * @param p
     */
    public void start(int p){
        numPlayers = p;
        
        //Assign roles
        ArrayList<Enum> roles = new ArrayList<Enum>();
        switch(p){
            case 2: //DEBUG MODE
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); break;
            case 4:
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); break;
            case 5:
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); 
                roles.add(Role.DEPUTY); break;
            case 6:
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); 
                roles.add(Role.DEPUTY); roles.add(Role.OUTLAW); break;
            case 7:
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); 
                roles.add(Role.DEPUTY); roles.add(Role.OUTLAW); 
                roles.add(Role.DEPUTY); break;
            case 8:
                roles.add(Role.SHERIFF); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); 
                roles.add(Role.DEPUTY); roles.add(Role.OUTLAW); 
                roles.add(Role.OUTLAW); roles.add(Role.RENEGADE); break;
            default: 
                System.out.print("Bad number of players!"); System.exit(0); break;
        }
        for(int n=0; n<numPlayers; n++){
            int role = roles.remove((int)(Math.random()*roles.size())).ordinal();
            server.sendInfo(n,"SetInfo:role:"+role);
            if(role==0){
                server.sendInfo(n,"SetInfo:maxHP:1");
            }
        }
        for(Card s: drawPile)
            System.out.print(s.name+" ");
        System.out.print("\n");
        
        //Assign character cards
        ArrayList<Enum> charList = new ArrayList<Enum>();
        for(Enum e: Characters.values()){
            charList.add(e);
        }
        for(int n = 0; n<numPlayers; n++){
            drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
            drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
            playerDrawCard(n, 2);
        }
        
        server.promptAll("ChooseCharacter");
        
        //debug mode


        if(p==2){
            
        }
        
        //Make players choose characters; wait
        /*for(int n = 0; n<numPlayers; n++){
            //doesn't prompt all players at the same time
            System.out.println("1. " + players[n].hand.get(0).name + " HP: " + players[n].hand.get(0).special);
            System.out.println("2. " + players[n].hand.get(1).name + " HP: " + players[n].hand.get(1).special);
            Card c = players[n].hand.get(gui[n].promptChooseCharacter(players[n].hand));
            players[n].character = c.ordinal;
            players[n].maxLifePoints += c.special; //special is hp for char cards
            players[n].lifePoints = players[n].maxLifePoints;
            playerDiscardHand(players[n]);
            gui[n].paint(gui[n].getGraphics()); //TODO: this shouldn't here, but this is the only place where it didn't glitch up
        }*/
        //System.out.println("asdfasdfasdfasfasdfasfasfasfasdf1");
        while(server.prompting>0){try{Thread.sleep(100);} catch(Exception e){}}
        //System.out.println("asdfasdfasdfasfasdfasfasfasfasdf");
        
        //Create a drawPile
        Enum[] cards = new Enum[120];
        Arrays.fill(cards, 0, 1, CardName.APPALOOSA);
        Arrays.fill(cards, 1, 30, CardName.BANG);
        Arrays.fill(cards, 30, 33, CardName.BARREL);
        Arrays.fill(cards, 33, 41, CardName.BEER);
        Arrays.fill(cards, 41, 42, CardName.BIBLE);
        Arrays.fill(cards, 42, 43, CardName.BRAWL);
        Arrays.fill(cards, 43, 44, CardName.BUFFALO_RIFLE);
        Arrays.fill(cards, 44, 45, CardName.CAN_CAN);
        Arrays.fill(cards, 45, 51, CardName.CAT_BALLOU);
        Arrays.fill(cards, 51, 52, CardName.CONESTOGA);
        Arrays.fill(cards, 52, 53, CardName.DERRINGER);
        Arrays.fill(cards, 53, 55, CardName.DODGE);
        Arrays.fill(cards, 55, 58, CardName.DUEL);
        Arrays.fill(cards, 58, 60, CardName.DYNAMITE);
        Arrays.fill(cards, 60, 61, CardName.GATLING);
        Arrays.fill(cards, 61, 64, CardName.GENERAL_STORE);
        Arrays.fill(cards, 64, 65, CardName.HOWITZER);
        Arrays.fill(cards, 65, 66, CardName.HOWITZER);
        Arrays.fill(cards, 66, 69, CardName.INDIANS);
        Arrays.fill(cards, 69, 71, CardName.IRON_PLATE);
        Arrays.fill(cards, 71, 74, CardName.JAIL);
        Arrays.fill(cards, 74, 75, CardName.KNIFE);
        Arrays.fill(cards, 75, 88, CardName.MISS);
        Arrays.fill(cards, 88, 91, CardName.MUSTANG);
        Arrays.fill(cards, 91, 96, CardName.PANIC);
        Arrays.fill(cards, 96, 97, CardName.PEPPERBOX);
        Arrays.fill(cards, 97, 98, CardName.PONY_EXPRESS);
        Arrays.fill(cards, 98, 99, CardName.PUNCH);
        Arrays.fill(cards, 99, 100, CardName.RAG_TIME);
        Arrays.fill(cards, 100, 102, CardName.REMINGTON);
        Arrays.fill(cards, 102, 104, CardName.REV_CARBINE);
        Arrays.fill(cards, 104, 105, CardName.SALOON);
        Arrays.fill(cards, 105, 108, CardName.SCHOFIELD);
        Arrays.fill(cards, 108, 109, CardName.SILVER);
        Arrays.fill(cards, 109, 110, CardName.SOMBRERO);
        Arrays.fill(cards, 110, 111, CardName.SPRINGFIELD);
        Arrays.fill(cards, 111, 113, CardName.STAGECOACH);
        Arrays.fill(cards, 113, 114, CardName.TEN_GALLON_HAT);
        Arrays.fill(cards, 114, 115, CardName.TEQUILA);
        Arrays.fill(cards, 115, 117, CardName.VOLCANIC);
        Arrays.fill(cards, 117, 118, CardName.WELLS_FARGO);
        Arrays.fill(cards, 118, 119, CardName.WHISKY);
        Arrays.fill(cards, 119, 120, CardName.WINCHESTER);
        
        ArrayList<Enum> allCards = new ArrayList<Enum>();
        for(Enum e: cards)
            allCards.add(e);
        while(allCards.size()>0){
            drawPile.add(new Card(allCards.remove((int)(Math.random()*allCards.size()))));
        }
        
        //draw cards equal to lifepoints
        for(Player p1: players){
            playerDrawCard(p1, p1.lifePoints);
        }
        
        gui[0].appendText("Cards in draw pile: ");
        String pile = "";
        for(Card s: drawPile)
            pile = pile + s.name + " "; //TODO: make large messages wrap around
        gui[0].appendText(pile);
        gui[0].appendText("\nCards in hand: ");
        for(Card s: players[0].hand)
            gui[0].appendText(s.name+" ");
        gui[0].appendText("\nYou are: " + Characters.values()[players[0].character] + ", the " + players[0].role.name() + "\n");
        gui[0].paint(gui[0].getGraphics());
        
        //Give Sheriff the first turn (turn 0)
        for(int n=0; n<p; n++){
            if(players[n].role==Role.SHERIFF){
                turn=n-1;
                break;
            }
        }
        while(nextTurn()){
        }
        System.out.println("GAME OVER");
    }
    
    //returns false if game is over
    public boolean nextTurn(){
        turn++;
        
        
        //check if player is dead
        int oldturn = turn;
        while(players[turn%numPlayers].lifePoints==0&&turn-oldturn<numPlayers){
            turn++;
        }
        
        //check jail/dynamite
        
        //draw two cards
        if(players[turn%numPlayers].specialDraw==0){ //TODO: get rid of specialDraw, move to a direct reference to character cards
            playerDrawCard(players[turn%numPlayers], 2);
        }
        else{
            //Yuck, there's alot of characters with this ability
        }
        
        int card = -2;
        while(card != -1){
            if(players[turn%numPlayers].hand.size()>0){
                if(isGameWon())
                    return false;
                card = gui[turn%numPlayers].promptChooseCard(players[turn%numPlayers].hand, "Play a card!", "It's your turn", false);
                if(card!=-1)
                    playCardFromHand(players[turn%numPlayers], players[turn%numPlayers].hand.get(card));
                
            }
            else{
                //normally, you'd still be able to play cards on field
                card = -1;
            }
        }
        return !isGameWon();
    }
    
    /**
     * If written, this method would replace the return value above
     */
    public boolean isGameWon(){
        int deputies = 0;
        int sheriff = 0;
        int outlaws = 0;
        int renegades = 0;
        for(Player p: players){
            if(p.lifePoints>0){
                switch((Role)p.role){
                    case DEPUTY: deputies++;
                    case SHERIFF: sheriff++; break;
                    case OUTLAW: outlaws++; break;
                    case RENEGADE: renegades++; break;
                }
            }
        }
        if(sheriff==0){
            if(outlaws==0&&deputies==0){
                System.out.println("Renegades win!");
                return true;
            }
            else{
                System.out.println("Outlaws win!");
                return true;
            }
        }
        if(sheriff>=0){
            if(outlaws==0&&deputies==0){
                System.out.println("Sheiff+Deputies win!");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Plays a card. This is one of the functions used to connect the GUI to the game.
     * @param c
     * @return Whether that is a legal move (boolean) //TODO: this is stupid
     */
    public boolean playCardFromHand(Player p, Card c){
        if(c.type == 4){
            System.out.println("A miss card cannot be played.");
            return false;
        }
        p.hand.remove(c);
        if(c.type == 3){
            //put it on the field
        }
        if(c.type == 2){
            int[] targets;
            if(c.target == 2){
                targets = new int[]{gui[p.id].promptChooseTargetPlayer()};
            }
            else if(c.target == 4){
                targets = new int[numPlayers-1];
                int m = 0;
                for(int n = 0; n<targets.length; n++, m++)
                    if(m!=p.id)
                        targets[n] = m;
                    else
                        n--;
            }
            else if(c.target ==3){
                targets = new int[numPlayers];
                for(int n = 0; n<targets.length; n++)
                    targets[n] = n;
            }
            else{
                targets = new int[1]; //serves no purpose but to initialize value
            }
        
            //damage
            if(c.effect == Card.play.DAMAGE.ordinal()){
                for(int target: targets){
                    int miss = -2; //or bang for indians
                    while(miss != -1 || (miss>=0 && miss<players[target].hand.size() && players[target].hand.get(miss).special==(c.name==CardName.INDIANS.name()?1:0) && players[target].hand.get(miss).effect==(c.name==CardName.INDIANS.name()?Card.play.DAMAGE.ordinal():Card.play.DAMAGE.ordinal()))){
                        miss = gui[target].promptChooseCard(players[target].hand, "Dodge!", "Play a miss?", false);
                    }
                    if(miss == -1){ //change this to a flag checking barrels/if target want to play a miss, etc.
                        while(players[target].lifePoints<=0){
                            System.out.println("Invalid Target!");
                            gui[p.id].promptChooseTargetPlayer();
                        }
                        players[target].lifePoints--;
                        System.out.println(target+"'s hp: "+players[target].lifePoints);
                        if(players[target].lifePoints <= 0){
                            gui[p.id].appendText("You killed player "+target+"! \nPlayer "+target+" was a(n) "+players[target].role.name()+" ("+players[target].role.ordinal()+")");
                            if(players[target].role.ordinal()==2){ //if he was an outlaw, claim bounty
                                gui[p.id].appendText("Draw 3 cards!");
                                playerDrawCard(p, 3);
                            }
                        }
                    }
                }
            }
            
            //heal
            if(c.effect == Card.play.HEAL.ordinal()){
                for(int target: targets){
                    if(c.range != 1) //shiskey
                        players[target].lifePoints++;
                    else
                        players[target].lifePoints += 2;
                    if(players[target].lifePoints>players[target].maxLifePoints)
                        players[target].lifePoints = players[target].maxLifePoints;
                    System.out.println(target+"'s hp: "+players[target].lifePoints);
                }
            }
            //draw
            if(c.effect == Card.play.DRAW.ordinal())
                playerDrawCard(p, c.range);
            if(c.effect2 != 0 && c.effect2 == Card.play.DRAW.ordinal())
                playerDrawCard(p, 1);
            discardPile.add(c);
        }
        //TODO: currently only removes the card from hand and sets it into discard
        return true;
    }
    
    public void playerDrawCard(int p, int n){
        for(int m=0; m<n; m++){
            Card c = drawCard();
            server.sendInfo(p, "Draw:"+(c.type==1?"Character:":"Game:")+c.name);
        }
    }
    
    //this one won't work anymore
    public void playerDrawCard(Player p, int n){
        /*if(n <= 2)
            gui[p.id].appendText("Draw "+n+" card(s).");
        else if(n>2)
            gui[p.id].appendText("Draw "+n+" cards!");*/
        for(int m=0; m<n; m++)
            server.sendInfo(p.id, drawCard().toString());
    }
    
    /**
     * Discards Player p's hand
     */
    public void playerDiscardHand(Player p){
        for(int n=p.hand.size()-1; n>=0; n--)
            playerDiscardCard(p, n);
    }
    
    /**
     * Discards card n in Player p's hand
     */
    public void playerDiscardCard(Player p, int n){
        Card c = p.hand.get(n);
        //is card a character card
        if(c.type==1){
            p.hand.remove(c);
        }
        else{
            discardPile.add(c);
        }
            
    }
    
    /**
     * Flips the top card of the drawPile. This card is then put in the discard 
     * pile. Used for barrels and other effects.
     * @return
     */
    public Card flipCard(){
        Card c = drawCard();
        discardPile.add(drawCard());
        return c;
    }
    
    /**
     * Draws one card. This card is either returned to the flipCard method or
     * the playerDrawCard method.
     * @param
     * @return Card
     */
    public Card drawCard(){
        if(drawPile.size()==0){
            shuffleDeck();
        }
        return drawPile.remove(drawPile.size()-1);
    }
    
    /**
     * Shuffles the discard pile back into the deck.
     * Only used when draw pile is empty
     */
    public void shuffleDeck(){
        if(drawPile.size()>0){
            System.out.println("Error: did not need to shuffleDeck()");
            return;
        }
        while(discardPile.size()>0){
            drawPile.add(discardPile.remove((int)Math.random()*discardPile.size()));
        }
    }
}
