package relation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import relation.predicat.ArbrePredicat;
import relation.predicat.NoeudPredicat;
import relation.syntaxe.Syntaxe;

public class LRSInterpretteur {

    private final HashMap<String, String> COMMANDES_VALIDES = new HashMap<String, String>();
    private final boolean DEBUG = true;
    private String CURRENT_DIR;

    private Base base = new Base();
    private Scanner sc = new Scanner(System.in);
    private StringBuilder inputBuffer = new StringBuilder();
    private boolean en_marche = true;

    // Bloc d'initialisation
    {
        COMMANDES_VALIDES.put("CREER RELATION", "creer");
        COMMANDES_VALIDES.put("AJOUTER DANS", "inserer");
        COMMANDES_VALIDES.put("CHOISIR", "selectionner");
        COMMANDES_VALIDES.put("QUITTER", "quitter");
        COMMANDES_VALIDES.put("AIDE", "aide");
    }

    public LRSInterpretteur() {
        // Obtenez la classe courante
        Class<?> classeCourante = this.getClass();

        // Obtenez le ClassLoader de la classe courante
        ClassLoader classLoader = this.getClass().getClassLoader();

        // Obtenez le chemin absolu du répertoire contenant la classe courante
        String chemin = classLoader.getResource(classeCourante.getName().replace('.', '/') + ".class").getPath();
        CURRENT_DIR = chemin.substring(0, chemin.lastIndexOf('/'));
        // RACINE = repertoire.substring(0, chemin.lastIndexOf('/'));

        accueil();
        demarrer();
    }

    private void afficherFichier(String cheminFichier) {
        try {
            // Créez un objet FileReader pour lire le fichier
            FileReader fileReader = new FileReader(cheminFichier);

            // Créez un objet BufferedReader pour lire le fichier ligne par ligne
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String ligne;
            // Lisez chaque ligne du fichier et affichez-la dans la console
            while ((ligne = bufferedReader.readLine()) != null) {
                System.out.println(ligne);
            }

            // Fermez le BufferedReader et le FileReader
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // e.printStackTrace();
        }
    }

    private void accueil() {
        // Spécifiez le chemin du fichier que vous souhaitez lire
        String cheminFichier = CURRENT_DIR + "/../../assets/accueil.txt";

        afficherFichier(cheminFichier);
    }

    private void demarrer() {
        while (en_marche) {
            System.out.print("\t#=====>");
            String ligne = sc.nextLine();
            ligne = ligne.trim();

            // N'ajoute pas les lignes commentées
            if (!ligne.startsWith("--")) {
                inputBuffer.append(ligne).append(" ");    
            }

            if(ligne.contains(";") && !inputBuffer.toString().trim().isEmpty()) {
                String commande = inputBuffer.toString().trim();
                inputBuffer.setLength(0);
                try {
                    interpretter(commande.trim());
                } catch (Throwable e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    } else {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }

    private void interpretter(String commande) throws Throwable {
        String nomMethode = "", cle = "";
        // Met le nom de la méthode à appeler dans la variable nomMethode si la commande correspond à une des commandes valides
        for (String commandeValide : COMMANDES_VALIDES.keySet()) {
            if (commande.toUpperCase().startsWith(commandeValide)) {
                nomMethode = COMMANDES_VALIDES.get(commandeValide);
                cle = commandeValide;
            }
        }

        if (nomMethode.equals("")) {
            String[] mots = commande.split(" ");
            if (mots.length >= 2) {
                throw new Exception("Commande " + mots[0] + " " + mots[1] + " inconnue");
            } else {
                throw new Exception("Commande " + mots[0] + " inconnue");
            }
        } else {
            try {
                Method methode = this.getClass().getDeclaredMethod(nomMethode, String.class);
                
                // Invoque la méthode correpsondant à la commande tout en retirant le début de la commande
                // CREER RELATION relation1 : id entier => relation1 : id entier
                methode.invoke(this, commande.substring(cle.length()));
            } catch (java.lang.reflect.InvocationTargetException e) {
                // System.err.println("Erreur lors de l'invocation de la méthode " + nomMethode);
                throw e.getCause();
            }
        }
    }

    public void inserer(String commande) throws Exception {
        String commande2 = commande.substring(0, commande.indexOf(";")).trim();
        Vector<String> parties = Syntaxe.separerHG(commande2, ":");
        // S'il n'y a qu'un seul ":" dans la commande
        if (parties.size() == 2) { // Les données à insérer
            Vector<Vector<Object>> nouvellesDonnees = new Vector<Vector<Object>>();
            String info_relation = parties.get(0);
            String donnees_non_formattees = parties.get(1);

            if(!Syntaxe.contientHG(donnees_non_formattees, "[") || !Syntaxe.contientHG(donnees_non_formattees, "]")) {
                throw new Exception("Chaque nouvelle ligne doit être entourée par des crochets comme ceci : [valeur1, valeur2]");
            } else if (Syntaxe.compterCaractereHG(donnees_non_formattees, '[') != Syntaxe.compterCaractereHG(donnees_non_formattees, ']')) {
                throw new Exception("Il y a des crochets en trop dans les données à insérer");
            } else {
                Vector<String> partiesHG = Syntaxe.obtenirPartiesHGHC(donnees_non_formattees);
                for (int i = 0; i < partiesHG.size(); i++) {
                    if (partiesHG.get(i).trim().equals("")) {
                        partiesHG.remove(i);
                    }
                }
                if (partiesHG.size() > 0) {
                    throw new Exception("Chaque nouvelle ligne doit être entourée par des crochets");
                } else {
                    Vector<String> lignes = Syntaxe.obtenirPartiesHGDansC(donnees_non_formattees);
                    for (int j = 0; j < lignes.size(); j++) {
                        Vector<Object> vectorLigne = new Vector<Object>();
                        Vector<String> cellules = Syntaxe.separerHG(lignes.get(j).substring(1, lignes.get(j).length() - 1), ",");
                        for (int i = 0; i < cellules.size(); i++) {
                            String cellule = cellules.get(i).trim();
                            if (!cellule.equals("")) {
                                vectorLigne.add(cellule);
                            }
                        }
                        nouvellesDonnees.add(vectorLigne);
                    }
                }
            }

            if (info_relation.contains("[") && info_relation.contains("]")) {
                if (Syntaxe.obtenirPartiesHGDansC(info_relation).size() == 1) {
                    Vector<String> nom_colonnes = Syntaxe.separerHG(
                        Syntaxe.obtenirPartiesHGDansC(info_relation).get(0).substring(
                            1,
                            Syntaxe.obtenirPartiesHGDansC(info_relation).get(0).length() - 1
                        ),
                        ","
                    );
                    if (Syntaxe.obtenirPartiesHGHC(info_relation).size() == 1) {
                        String nom_relation = Syntaxe.obtenirPartiesHGHC(info_relation).get(0).trim();
                        if (nom_relation.contains(" ")) {
                            throw new Exception("Le nom de relation " + nom_relation + " est invalide");
                        } else {
                            base.inserer(nom_relation, nouvellesDonnees, nom_colonnes);
                        }
                    } else {
                        throw new Exception("Il y a une erreur de syntaxe dans les informations concernant la relation : " + info_relation);
                    }
                } else {
                    throw new Exception("Il y a des guillemets en trop dans : " + info_relation);
                }
            } else {
                if (info_relation.contains(" ")) {
                    throw new Exception("Le nom de relation " + info_relation + " est invalide");
                } else {
                    base.inserer(info_relation, nouvellesDonnees);
                }
            }
        } else if(parties.size() < 2) {
            throw new Exception("Symbôle \":\" attendu après AJOUTER DANS " + parties.get(0));
        } else {
            throw new Exception("Erreur de syntaxe près de \":" + parties.get(2) + "\"");
        }
    }
    
    public Relation selectionnerCombinaison(String commande) throws Exception {
        String comOp = "";
        Vector<String> sousRequetes = null;
        String sousRequete = null;
        Relation resultat = null;
        if (Syntaxe.contientHGHP(commande.toUpperCase(), " INTERSECTION ")) {
            comOp = "INTERSECTION";
        } else if (Syntaxe.contientHGHP(commande.toUpperCase(), " DIFFERENCE ")) {
            comOp = "DIFFERENCE";            
        } else {
            comOp = "UNION";
        }

        sousRequetes = Syntaxe.separerHGHP(commande, comOp);
        resultat = selectionner(sousRequetes.get(0), false);

        for (int i = 1; i < sousRequetes.size(); i++) {
            sousRequete = sousRequetes.get(i);
            switch (comOp) {
                case "INTERSECTION":
                    resultat = resultat.intersection(selectionner(sousRequete, false));
                    break;
            
                case "DIFFERENCE":
                    resultat = resultat.difference(selectionner(sousRequete, false));
                    break;
            
                case "UNION":
                    resultat = resultat.union(selectionner(sousRequete, false));
                    break;
            
                default:
                    break;
            }
        }

        return resultat;
    }

    public Relation selectionner(String commande) throws Exception {
        return selectionner(commande, true);
    }

    public Relation selectionner(String commande, boolean afficher) throws Exception {
        // Supprime les points-virgules et espaces en excédent
        String commandeTraitee = Syntaxe.remplacerHG(commande, ";", "").trim();
        Relation resultat = null;
        ArbrePredicat predicats = null;
        int limite = -1;

        if (commandeTraitee.toUpperCase().startsWith("CHOISIR ")) {
            commandeTraitee = commandeTraitee.substring("CHOISIR ".length());
        }

        if (
            Syntaxe.contientHGHP(commande.toUpperCase(), " UNION ") ||
            Syntaxe.contientHGHP(commande.toUpperCase(), " INTERSECTION ") ||
            Syntaxe.contientHGHP(commande.toUpperCase(), " DIFFERENCE ")
        ) {
            resultat = selectionnerCombinaison(commandeTraitee);
        } else {
            // Vérifie la présence de "LIMITE" dans la commande en dehors des parenthèses
            if (Syntaxe.contientHGHP(commandeTraitee.toUpperCase(), " LIMITE ")) {
                Vector<String> partieLimites = Syntaxe.separerHGHP(commandeTraitee, " LIMITE ");
        
                // Vérifie qu'il y a au plus une occurrence de "LIMITE"
                if (partieLimites.size() > 2) {
                    throw new Exception("La commande ne doit pas contenir plusieurs symboles : LIMITE");
                } else {
                    commandeTraitee = partieLimites.get(0);
                    try {
                        limite = Integer.parseInt(partieLimites.get(1).trim());
                        commandeTraitee = partieLimites.get(0).trim();
                    } catch (Exception e) {
                        throw new Exception("Limite invalide : \"" + partieLimites.get(1).trim() + "\"");
                    }
                }
            }
        
            // Sépare la partie avant "PREDICATS" de la commande
            Vector<String> temps = Syntaxe.separerHGHP(commandeTraitee, " PREDICATS ");
            String avantPredicats = temps.get(0);
        
            // Vérifie qu'il n'y a au plus une occurrence de "PREDICATS"
            if (temps.size() > 2) {
                throw new Exception("La commande ne doit pas contenir plusieurs symboles : PREDICATS");
            } else if (temps.size() == 2) {
                // Crée un arbre de prédicats si nécessaire
                predicats = new ArbrePredicat(new NoeudPredicat(temps.get(1)));
                predicats.arrangerRacine();
            }

            // Vérifie la présence de parenthèses
            if (Syntaxe.contientHG(avantPredicats,"(") || Syntaxe.contientHG(avantPredicats,")")) {
                // Vérifie si les parenthèses sont correctement écrites, lance une exception en cas d'erreur
                if (Syntaxe.verifierParenthesesHG(avantPredicats)) {
                    // Indice de la parenthèse ouvrante
                    int indiceOuvrant = Syntaxe.indiceCarOHG(avantPredicats, '(');
                    // Indice de la parenthèse fermant la première parenthèse ouvrante
                    int indiceFermant = Syntaxe.indiceCarFHG(avantPredicats, '(', ')');

                    // Si la parenthèse ouvrante se situe bien en début de chaîne et la fermante en fin de chaîne
                    if (indiceOuvrant == 0 && indiceFermant == avantPredicats.length() - 1) {
                        Relation sousRelation = selectionner(avantPredicats.substring(1 + "CHOISIR".length(), indiceFermant).trim(), false);
                        resultat = base.selectionner(limite, sousRelation, predicats);
                    } else if (indiceOuvrant != 0) {
                        throw new Exception("Symbôle inconnu près de : " + avantPredicats.substring(0, indiceOuvrant + 1));
                    } else {
                        throw new Exception("Opérateur manquant près de : " + avantPredicats);
                    }
                }
            } else if (avantPredicats.contains("[") && avantPredicats.contains("]")) {
                // Vérifie la présence de crochets

                // Vérifie que le nombre de crochets ouverts et fermés est égal
                if (Syntaxe.compterCaractereHG(avantPredicats, '[') != Syntaxe.compterCaractereHG(avantPredicats, ']')) {
                    throw new Exception("Il y a des guillemets en trop dans : " + avantPredicats);
                } else {
                    String[] colonnes = null;
                    String nomRelation = avantPredicats;
        
                    // Traite les crochets pour extraire les colonnes et le nom de la relation
                    if (avantPredicats.endsWith("]")) {
                        colonnes = avantPredicats.substring(avantPredicats.lastIndexOf("[") + 1, avantPredicats.lastIndexOf("]")).split(",");
                        nomRelation = avantPredicats.substring(0, avantPredicats.lastIndexOf("[")).trim();
                    }
        
                    // Si des colonnes sont présentes, les trim et les assigne
                    if (colonnes != null) {
                        for (int i = 0; i < colonnes.length; i++) {
                            colonnes[i] = colonnes[i].trim();
                        }
                    }
        
                    // Appelle la méthode de sélection avec les paramètres correspondants
                    resultat = base.selectionner(limite, nomRelation, predicats, colonnes);
                }
            } else if (!avantPredicats.contains("[") && !avantPredicats.contains("]")) {
                // Si aucun crochet n'est présent, appelle la méthode de sélection avec les paramètres correspondants
                String nomRelation = avantPredicats.trim();
                resultat = base.selectionner(limite, nomRelation, predicats);
            } else {
                // En cas d'erreur de syntaxe avec les crochets ou les parenthèses, lance une exception
                String pattern = commandeTraitee.contains("[") ? "[" : "]";
                throw new Exception("Erreur de syntaxe près de " + commande.substring(0, commande.indexOf(pattern) + 1));
            }
        }

        if (afficher) {
            resultat.afficherDonnees();
        }

        return resultat;
    }

    public void creer(String commande) throws Exception {
        String commande2 = Syntaxe.remplacerHG(commande, ";", "").trim();
        Vector<String> parties = Syntaxe.separerHG(commande2, ":");
        // S'il n'y a qu'un seul ":" dans la commande
        if (parties.size() == 2) {
            String nom_relation = parties.get(0).trim();
            // Vérifie si le nom de la relation contient un espace
            if (Syntaxe.contientCarSpec(nom_relation) || Syntaxe.estMotCle(nom_relation)) {
                throw new Exception("Nom de relation \"" + nom_relation + "\" invalide !");
            // Vérifie si le nom de la relation est une chaîne vide
            } else if (nom_relation.equals("")) {
                throw new Exception("Nom de relation manquante");
            } else {
                // Sépare les différentes colonnes
                String[] sequences = parties.get(1).split(",");
                Vector<String> colonnes = new Vector<String>();
                Vector<String> domaines = new Vector<String>();
                for (String sequence : sequences) {
                    // Sépare le nom de colonne et le domaine
                    String[] infos = sequence.trim().split(" ");
                    if (infos.length > 2) {
                        throw new Exception("Erreur de syntaxe près de \"" + infos[2] + "\"");
                    } else if(infos.length <= 1) {
                        throw new Exception("Nom de colonne ou domaine manquant près de " + infos[0]);
                    } else {
                        colonnes.add(infos[0].trim());
                        domaines.add(infos[1].trim());
                    }
                }
                base.creerRelation(nom_relation, colonnes, domaines);
            }
        } else if(parties.size() < 2) {
            // Si la commande se termine par ":" alors il n'y a aucune instruction après ce symbôle
            if (commande2.endsWith(":")) {
                throw new Exception("Colonnes et domaines manquants après \":\"");
            } else {
                throw new Exception("Symbôle \":\" attendu après CREER RELATION " + parties.get(0));
            }
        } else {
            throw new Exception("Erreur de syntaxe près de \"" + commande.substring(commande.lastIndexOf(":")) + "\"");
        }
    }

    public void quitter(String commande) throws Exception {
        String reste = commande.replace(";", "").trim();
        if (reste.equals("")) {
            en_marche = false;
            System.out.println("A bientôt!");
        } else {
            throw new Exception("Erreur de syntaxe près de " + reste);
        }
    }

    public void aide(String commande) throws Exception {
        String reste = commande.replace(";", "").trim();
        if (reste.equals("")) {
            System.out.println();
            String cheminFichier = CURRENT_DIR + "/../../assets/aide.txt";

            afficherFichier(cheminFichier);
        } else {
            throw new Exception("Erreur de syntaxe près de " + reste);
        }
    }

}