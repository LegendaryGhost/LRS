package relation;

import java.util.Vector;

import relation.operation.NoeudOperation;
import relation.predicat.ArbrePredicat;
import relation.predicat.NoeudPredicat;
import relation.syntaxe.Syntaxe;

public class Relation {

    private String nom;
    private Vector<String> colonnes = new Vector<String>();
    private Vector<String> domaines = new Vector<String>();
    private Vector<Vector<Object>> donnees = new Vector<Vector<Object>>();

    private final String[] DOMAINES_VALIDES = {
        "entier",
        "decimal",
        "lettres",
        "booleen"
    };
    private final int MAX_COL_LONG = 20,
        MIN_COL_LONG = 1;
    private final String [] OPERATEURS = {
        "=", "<", ">", "<=", ">=", "!="
    };

    private Vector<String> colonnes_introuvables;
    private Vector<String> domaines_invalides;

    public Relation(String nom) {
        this.nom = nom;
    }

    public Vector<Vector<Object>> getDonnees() {
        return donnees;
    }

    public void setDonnees(Vector<Vector<Object>> donnees) {
        this.donnees = donnees;
    }

    public Vector<String> getColonnes() {
        return colonnes;
    }

    public void setColonnes(Vector<String> colonnes) {
        setColonnes(colonnes, false);
    }

    public void setColonnes(Vector<String> colonnes, boolean verif) {
        if (verif) {
            for (String nom_col : colonnes) {
                if (Syntaxe.contientCarSpec(nom_col) || Syntaxe.estMotCle(nom_col)) {
                    throw new IllegalArgumentException("Nom de colonne \"" + nom_col + "\" invalide");
                }
            }
        }
        this.colonnes = colonnes;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Vector<String> getDomaines() {
        return domaines;
    }

    public void setDomaines(Vector<String> domaines) throws Exception {
        if (!domainesValides(domaines)) {
            String domaines_formattees = domaines_invalides.get(0);
            for (int i = 1; i < domaines_invalides.size(); i++) {
                domaines_formattees += ", " + domaines_invalides.get(i);
            }
            throw new Exception("Domaine(s) [" + domaines_formattees + "] invalide(s)");
        } else {
            this.domaines = domaines;
        }
    }

    private boolean domainesValides(Vector<String> domaines) {
        boolean resultat = true;
        domaines_invalides = new Vector<String>();
        for (String domaine : domaines) {
            if (!tabChaineContient(DOMAINES_VALIDES, domaine)) {
                domaines_invalides.add(domaine);
                resultat = false;
            }
        }
        return resultat;
    }

    private boolean tabChaineContient(String[] tableau, String chaine) {
        for (String element : tableau) {
            if (element.equals(chaine)) {
                return true;
            }
        }
        return false;
    }

    private boolean domainesSimilaires(Vector<String> domaines2) {
        for (int i = 0; i < domaines.size(); i++) {
            if (!domaines.get(i).equals(domaines2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean lignesSimilaires(Vector<Object> ligne1, Vector<Object> ligne2) {
        for (int i = 0; i < ligne1.size(); i++) {
            if(!ligne1.get(i).equals(ligne2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean donneesContient(Vector<Vector<Object>> donnees, Vector<Object> ligne2) {
        for (Vector<Object> ligne : donnees) {
            if(lignesSimilaires(ligne, ligne2)) {
                return true;
            }
        }
        return false;
    }

    private Vector<Vector<Object>> donneesSansDoublons(Vector<Vector<Object>> donnees2) {
        Vector<Vector<Object>> resultat = new Vector<Vector<Object>>();

        for (Vector<Object> ligne : donnees) {
            resultat.add(ligne);
        }

        for (Vector<Object> ligne2 : donnees2) {
            if(!donneesContient(donnees, ligne2)) {
                resultat.add(ligne2);
            }
        }

        return resultat;
    }

    public Relation union(Relation relation2) throws Exception {
        if(this.colonnes.size() != relation2.getColonnes().size()) {
            throw new Exception("Le nombre de colonnes doit être le même dans les 2 relations");
        } else if(!domainesSimilaires(relation2.getDomaines())) {
            throw new Exception("Les domaines des 2 relations doivent être les mêmes");
        } else {
            Relation resultat = new Relation(nom + " UNION " + relation2.getNom());
            resultat.setColonnes(colonnes);
            resultat.setDomaines(domaines);
            resultat.setDonnees(donneesSansDoublons(relation2.getDonnees()));
            return resultat;
        }
    }

    public Relation intersection(Relation relation2) throws Exception {
        if(this.colonnes.size() != relation2.getColonnes().size()) {
            throw new Exception("Le nombre de colonnes doit être le même dans les 2 relations");
        } else if(!domainesSimilaires(relation2.getDomaines())) {
            throw new Exception("Les domaines des 2 relations doivent être les mêmes");
        } else {
            Relation resultat = new Relation(nom + " INTERSECTION " + relation2.getNom());
            resultat.setColonnes(colonnes);
            resultat.setDomaines(domaines);
            Vector<Vector<Object>> donneesResultat = new Vector<Vector<Object>>();
            for (Vector<Object> ligne : relation2.getDonnees()) {
                if (donneesContient(donnees, ligne)) {
                    donneesResultat.add(ligne);
                }
            }
            resultat.setDonnees(donneesResultat);

            return resultat;
        }
    }

    public Relation difference(Relation relation2) throws Exception {
        if(this.colonnes.size() != relation2.getColonnes().size()) {
            throw new Exception("Le nombre de colonnes doit être le même dans les 2 relations");
        } else if(!domainesSimilaires(relation2.getDomaines())) {
            throw new Exception("Les domaines des 2 relations doivent être les mêmes");
        } else {
            Relation resultat = new Relation(nom + " DIFFERENCE " + relation2.getNom());
            resultat.setColonnes(colonnes);
            resultat.setDomaines(domaines);
            Vector<Vector<Object>> donneesResultat = new Vector<Vector<Object>>();
            for (Vector<Object> ligne : relation2.getDonnees()) {
                if (!donneesContient(donnees, ligne)) {
                    donneesResultat.add(ligne);
                }
            }
            for (Vector<Object> ligne : donnees) {
                if (!donneesContient(relation2.getDonnees(), ligne)) {
                    donneesResultat.add(ligne);
                }
            }
            resultat.setDonnees(donneesResultat);

            return resultat;
        }
    }

    public Relation produitCart(Relation relation2) throws Exception {
        Vector<Relation> vRelation = new Vector<Relation>();
        vRelation.add(relation2);
        return produitCart(vRelation);
    }

    public Relation produitCart(Vector<Relation> relations) throws Exception {
        Relation resultat = new Relation(nom);
        Vector<String> nouvColonnes = new Vector<String>();
        Vector<String> nouvDomaines = new Vector<String>();
        Vector<Vector<Object>> nouvDonnees = donnees;

        for (Relation relation : relations) {
            resultat.setNom(resultat.getNom() + " x " + relation.getNom());
        }

        for (String colonne : colonnes) {
            nouvColonnes.add(nom + "." + colonne);
        }
        for (Relation relation : relations) {
            for (String colonne : relation.getColonnes()) {
                nouvColonnes.add(relation.getNom() + "." + colonne);
            }
        }
        

        for (String domaine : domaines) {
            nouvDomaines.add(domaine);
        }
        for (Relation relation : relations) {
            for (String domaine : relation.getDomaines()) {
                nouvDomaines.add(domaine);
            }
        }

        for (Relation relation : relations) {
            nouvDonnees = produitDonnees(nouvDonnees, relation.getDonnees());
        }

        resultat.setColonnes(nouvColonnes);
        resultat.setDomaines(nouvDomaines);
        resultat.setDonnees(nouvDonnees);

        return resultat;
    }

    private Vector<Vector<Object>> produitDonnees(Vector<Vector<Object>> donnees1, Vector<Vector<Object>> donnees2) {
        Vector<Vector<Object>> resultat = new Vector<Vector<Object>>();
        
        for (Vector<Object> ligne1 : donnees1) {
            for (Vector<Object> ligne2 : donnees2) {
                Vector<Object> nouvLigne = new Vector<Object>();
                nouvLigne.addAll(ligne1);
                nouvLigne.addAll(ligne2);
                resultat.add(nouvLigne);
            }
        }
        
        return resultat;
    }

    public Relation jointureNat(Relation relation2) throws Exception {
        Vector<Relation> vRelation = new Vector<Relation>();
        vRelation.add(relation2);
        return jointureNat(vRelation);
    }

    public Relation jointureNat(Vector<Relation> relations) throws Exception {
        Relation resultat = produitCart(relations);
        Vector<String> colsCommun = new Vector<String>();
        String predicats = "";

        resultat.setNom(nom);

        // Récupérer les colonnes en commun
        colsCommun.addAll(colonnes);
        for (Relation relation : relations) {
            // Modifier le nom de la relation résultat
            resultat.setNom(resultat.getNom() + " |X| " +  relation.getNom());
            Vector<String> nouvColsCommun = new Vector<String>();
            for (String colCom : colsCommun) {
                if(relation.getColonnes().contains(colCom)) {
                    nouvColsCommun.add(colCom);
                }
            }
            colsCommun = nouvColsCommun;
        }

        for (Relation relation : relations) {
            for (String colCom : colsCommun) {
                predicats += nom + "." + colCom + "=" + relation.getNom() + "." + colCom + " ET ";
            }
        }

        if (predicats.equals("")) {
            String message_erreur = "Aucune colonne en commun dans les tables : " + nom;
            for (Relation relation : relations) {
                message_erreur += ", " + relation.getNom();
            }
            throw new Exception(message_erreur);
        } else {
            predicats = predicats.substring(0, predicats.lastIndexOf(" ET "));
            ArbrePredicat ap = new ArbrePredicat(new NoeudPredicat(predicats));
            ap.arrangerRacine();
            resultat = ap.getRelation(resultat);
        }

        Vector<String> ancienneCols = resultat.getColonnes();
        Vector<String> nouvCols = new Vector<String>();
        for (String colCom : colsCommun) {
            nouvCols.add(nom + "." + colCom);
        }
        for (String ancienneCol : ancienneCols) {
            Boolean commun = false;
            for (String colCom : colsCommun) {
                if (ancienneCol.endsWith("." + colCom)) {
                    commun = true;
                    break;
                }
            }
            if (!commun) {
                nouvCols.add(ancienneCol);
            }
        }

        resultat = resultat.projection(nouvCols.toArray(new String[nouvCols.size()]));

        for (int i = 0; i < nouvCols.size(); i++) {
            nouvCols.set(i, nouvCols.get(i).substring(nouvCols.get(i).indexOf(".") + 1));
        }

        resultat.setColonnes(nouvCols);

        return resultat;
    }

    public Relation tetaJointure(Relation relation2, String predicat) throws Exception {
        Relation resultat = produitCart(relation2);

        ArbrePredicat ap = new ArbrePredicat(new NoeudPredicat(predicat));
        ap.arrangerRacine();
        resultat = ap.getRelation(resultat);

        return resultat;
    }

    public void ajouterLigne(Vector<Object> ligne) throws Exception {
        if (ligne.size() != colonnes.size()) {
            throw new Exception("Le nombre de colonne de la relation " + nom + " et celui de la nouvelle ligne ne doivent pas être différentes");
        } else if (ligneCorrespondDomaines(ligne)) {
            donnees.add(ligne);
        }
    }

    private boolean ligneCorrespondDomaines(Vector<Object> ligne) throws Exception {
        boolean resultat = true;
        for (int i = 0; i < ligne.size(); i++) {
            String domaine_actuel = domaines.get(i);
            Object cellule = ligne.get(i);
            if (ligne.get(i) != null)  {
                switch (domaine_actuel) {
                    case "entier":
                        if (cellule.toString().matches("-?\\d+")) {
                            ligne.set(i, Integer.parseInt((cellule.toString())));
                        } else {
                            resultat = false;
                        }
                        break;
                    case "decimal":
                        if (cellule.toString().matches("-?\\d+(\\.\\d+)?")) {
                            ligne.set(i, Double.parseDouble(cellule.toString()));
                        } else {
                            resultat = false;
                        }
                        break;
                    case "lettres":
                        if ((cellule.toString().startsWith("\"") && cellule.toString().endsWith("\""))) {
                            ligne.set(i, cellule.toString().substring(1, cellule.toString().length() - 1));
                        } else {
                            resultat = false;
                            throw new Exception("Les éléments dans le domaine lettres doivent être entourés pas des double guillemets comme ceci : \"valeur\"");
                        }
                        break;
                    case "booleen":
                        if (cellule.toString().equals("vrai") || cellule.toString().equals("faux")) {
                            ligne.set(i, cellule.toString().equals("vrai") ? true : false);
                        } else {
                            resultat = false;
                        }
                        break;
                }
                if (!resultat) {
                    throw new Exception(cellule.toString() + " ne correspond pas au domaine " + domaine_actuel);
                }
            }
        }
        return resultat;
    }

    public void ajouter(Vector<Object> ligne, Vector<String> colonnes) throws Exception {
        if (ligne.size() != colonnes.size()) {
            throw new Exception("Le nombre de colonne de la relation " + nom + " et celui de la nouvelle ligne ne doivent pas être différentes");
        } else if(!contientColonnes(colonnes.toArray(new String[colonnes.size()]))) {
            String colFormattees = colonnes_introuvables.get(0);
            for (int i = 1; i < colonnes_introuvables.size(); i++) {
                colFormattees += ", " + colonnes_introuvables.get(i);
            }
            throw new Exception("Colonne(s) [" + colFormattees + "] introuvale(s) dans " + nom);
        } else {
            Vector<Object> ligneFormattee = new Vector<Object>();
            for (String colonne : this.colonnes) {
                if (colonnes.contains(colonne)) {
                    ligneFormattee.add(ligne.get(colonnes.indexOf(colonne)));
                } else {
                    ligneFormattee.add(null);
                }
            }
            this.ajouterLigne(ligneFormattee);
        }
    }

    public boolean contientColonnes(String[] colonnes) {
        boolean resultat = true;
        colonnes_introuvables = new Vector<String>();
        for (String colonne : colonnes) {
            if(!this.colonnes.contains(colonne)) {
                resultat = false;
                colonnes_introuvables.add(colonne);
            }
        }
        return resultat;
    }

    public void afficherDonnees() throws Exception {
        int[] maxColsLong = new int[colonnes.size()];
        for (int i = 0; i < colonnes.size(); i++) {
            maxColsLong[i] = getMaxColLongueur(colonnes.get(i));
        }
        System.out.println("Relation " + nom);

        afficherSeparation(maxColsLong);

        System.out.print("|");
        for (int i = 0; i < colonnes.size(); i++) {
            afficherCellule(colonnes.get(i), maxColsLong[i]);
        }
        System.out.println();
        
        afficherSeparation(maxColsLong);
        
        for (Vector<Object> ligne : donnees) {
            System.out.print("|");
            for (int i = 0; i < colonnes.size(); i++) {
                afficherCellule(ligne.get(this.colonnes.indexOf(colonnes.get(i))), maxColsLong[i]);
            }
            System.out.println();
        }
        afficherSeparation(maxColsLong);
    }

    public Relation projection(String[] colonnes) throws Exception {
        Relation resultat = new Relation(this.nom);
        if(!contientColonnes(colonnes)) {
            String colFormattees = colonnes_introuvables.get(0);
            for (int i = 1; i < colonnes_introuvables.size(); i++) {
                colFormattees += ", " + colonnes_introuvables.get(i);
            }
            throw new Exception("Colonne(s) [" + colFormattees + "] introuvale(s) dans " + nom);
        } else {
            Vector<String> nouv_colonnes = new Vector<String>();
            Vector<String> nouv_domaines = new Vector<String>();
            Vector<Vector<Object>> nouv_donnees = new Vector<Vector<Object>>();
            for (String colonne : colonnes) {
                nouv_colonnes.add(colonne);
                nouv_domaines.add(domaines.get(this.colonnes.indexOf(colonne)));
            }

            for (Vector<Object> ligne : donnees) {
                Vector<Object> nouv_ligne = new Vector<Object>();
                for (String colonne : colonnes) {
                    nouv_ligne.add(ligne.get(this.colonnes.indexOf(colonne)));
                }
                nouv_donnees.add(nouv_ligne);
            }

            resultat.setColonnes(nouv_colonnes);
            resultat.setDomaines(nouv_domaines);
            resultat.setDonnees(nouv_donnees);
        }
        return resultat;
    }

    /**
     * Prend un seul bloc de prédicat à la fois en paramètre : terme1 operateur terme2
     * Si vous voulez combiner plusieurs blocs de prédicat : [terme1 operateur terme2 OU terme3 operateur terme4] ET terme5 operateur terme6
     * pensez à utiliser les classes relation.predicat.ArbrePredicat et relation.predicat.NoeudPredicat
     * @param predicat un bloc de prédicat : terme1 operateur terme2
     * @return une relation dont les données correspondent au prédicat passé en paramètre
     * @throws Exception
     */
    public Relation predicat(String predicat) throws Exception {
        Relation resultat = new Relation(this.nom);
        resultat.setColonnes(colonnes);
        resultat.setDomaines(domaines);
        String operateur_courrant = "";
        for (String operateur : OPERATEURS) {
            if (Syntaxe.contientHG(predicat, operateur)) {
                operateur_courrant = operateur;
            }
        }

        if (operateur_courrant.equals("")) {
            throw new Exception("Opérateur inconnu ou manquant dans : " + predicat);
        } else {
            Vector<String> termes = Syntaxe.separerHG(predicat, operateur_courrant);
             if (termes.size() > 2) {
                throw new Exception("Un prédicat ne peut pas contenir plusieurs opérateurs à la fois : " + predicat);
            } else if (termes.size() < 2) {
                throw new Exception("Deux termes doivent se trouver avant et après chaque opérateur : " + predicat);
            } else {
                String type_terme1 = getTypeTerme(termes.get(0), predicat),
                    type_terme2 = getTypeTerme(termes.get(1), predicat);

                if (type_terme1.equals("colonne")) {
                    if (!colonnes.contains(termes.get(0))) {
                        throw new IllegalArgumentException("Colonne " + termes.get(0) + " inconnue près de : " + predicat);
                    }
                }

                if (type_terme2.equals("colonne")) {
                    if (!colonnes.contains(termes.get(1))) {
                        throw new IllegalArgumentException("Colonne " + termes.get(1) + " inconnue près de : " + predicat);
                    }
                }

                Vector<Vector<Object>> nouv_donnees = new Vector<Vector<Object>>();
                for (Vector<Object> ligne : donnees) {
                    if (ligneValidePredicat(ligne, termes.get(0), termes.get(1), type_terme1, type_terme2, operateur_courrant, predicat)) {
                        nouv_donnees.add(ligne);
                    }
                }
                resultat.setDonnees(nouv_donnees);
            }
        }
        return resultat;
    }

    public static String getTypeTerme(String terme) {
        return getTypeTerme(terme, "");
    }

    public static String getTypeTerme(String terme, String predicat) {
        if (
            terme.equals("")
        ) {
            throw new IllegalArgumentException("Terme manquant dans : " + predicat);
        // "chaîne"
        } else if (Syntaxe.contientOperateur(terme)) {
            return "expression";
        } else if (terme.startsWith("\"") && terme.endsWith("\"")) {
            return "lettres";
        // -678
        } else if (terme.matches("-?\\d+")) {
            return "entier";
        // -678.908
        } else if (terme.matches("-?\\d+(\\.\\d+)?")) {
            return "decimal";
        } else if (terme.equals("vrai") || terme.equals("faux")) {
            return "booleen";
        } else if (terme.equals("null")) {
            return "null";
        // [1 + 2] * 3
        } else {
            return "colonne";
        }
    }

    private boolean ligneValidePredicat(Vector<Object> ligne, String terme1, String terme2, String type_terme1, String type_terme2, String operateur, String predicat) throws Exception {
        boolean resultat = false;
        if (type_terme1.equals("lettres")) {
            terme1 = terme1.substring(1, terme1.length() - 1);
        } else if (type_terme1.equals("colonne")) {
            int col_id = colonnes.indexOf(terme1);
            Object terme_courrant = ligne.get(col_id);
            if (terme_courrant instanceof Boolean) {
                terme1 = ((Boolean)terme_courrant) ? "vrai" : "faux";
                type_terme1 = "booleen";
            } else if (terme_courrant ==  null) {
                type_terme1 = "null";
                terme1 = "null";
            } else {
                terme1 = terme_courrant.toString();
                type_terme1 = domaines.get(col_id);
            }
        } else if (type_terme1.equals("expression")) {
            // 1 + 1 - 1 => 1
            // "string1" + "string2" => "string1string2"
            // 1 + "string2" => erreur
            // id + 1 => 1 + 1 => 2
            // "string" + nom => "string" + "Tiarintsoa" => "stringTiarintsoa"
            // id + nom => erreur
            // nom - nom => erreur
            NoeudOperation noeudOp = new NoeudOperation(terme1, ligne, colonnes, domaines);
            noeudOp.arranger();
            terme1 = noeudOp.getValeurExpression();
            type_terme1 = noeudOp.getType();
        }

        if (type_terme2.equals("lettres")) {
            terme2 = terme2.substring(1, terme2.length() - 1);
        } else if (type_terme2.equals("colonne")) {
            int col_id = colonnes.indexOf(terme2);
            Object terme_courrant = ligne.get(col_id);
            if (terme_courrant instanceof Boolean) {
                terme2 = ((Boolean)terme_courrant) ? "vrai" : "faux";
                type_terme2 = "booleen";
            } else if (terme_courrant ==  null) {
                terme2 = "null";
                type_terme2 = "null";
            } else {
                terme2 = terme_courrant.toString();
                type_terme2 = domaines.get(col_id);
            }
        } else if (type_terme2.equals("expression")) {
            // 1 + 1 - 1 => 1
            // "string1" + "string2" => "string1string2"
            // 1 + "string2" => erreur
            // id + 1 => 1 + 1 => 2
            // "string" + nom => "string" + "Tiarintsoa" => "stringTiarintsoa"
            // id + nom => erreur
            // nom - nom => erreur
            NoeudOperation noeudOp = new NoeudOperation(terme2, ligne, colonnes, domaines);
            noeudOp.arranger();
            terme2 = noeudOp.getValeurExpression();
            type_terme2 = noeudOp.getType();
        }

        if (operateur.equals("=")) {
            terme1 += (type_terme2.equals("decimal") && type_terme1.equals("entier")) ? ".0" : ""; 
            terme2 += (type_terme1.equals("decimal") && type_terme2.equals("entier")) ? ".0" : ""; 
            resultat = terme1.equals(terme2);
        } else if (operateur.equals("!=")) {
            terme1 += (type_terme2.equals("decimal") && type_terme1.equals("entier")) ? ".0" : ""; 
            terme2 += (type_terme1.equals("decimal") && type_terme2.equals("entier")) ? ".0" : ""; 
            resultat = !terme1.equals(terme2);
        } else {
            // Si un des termes est null alors on dit que la condition est invalide
            if (type_terme1.equals("null") || type_terme2.equals("null")) {
                resultat = false;
            // Vérifie si les termes ne sont ni des entiers ni des décimaux
            } else if (
                (!(type_terme1.equals("entier")) && !(type_terme1.equals("decimal"))) ||
                (!(type_terme2.equals("entier")) && !(type_terme2.equals("decimal")))
            ) {
                throw new IllegalArgumentException("Type invalide pour l'opérateur " + operateur + " dans : " + predicat);
            } else {
                Double valeur1 = Double.parseDouble(terme1), valeur2 = Double.parseDouble(terme2);
                switch (operateur) {
                    case "<":
                        resultat = valeur1 < valeur2;
                        break;
                    case ">":
                        resultat = valeur1 > valeur2;
                        break;
                    case "<=":
                        resultat = valeur1 <= valeur2;
                        break;
                    case ">=":
                        resultat = valeur1 >= valeur2;
                        break;
                }
            }
        }
        return resultat;
    }

    private int getMaxColLongueur(String colonne) {
        int longueur = colonne.length(),
            idColonne = this.colonnes.indexOf(colonne);

        for (Vector<Object> ligne : donnees) {
            int longueurLigne = ligne.get(idColonne) == null ? 4 : ligne.get(idColonne).toString().length();
            if (longueurLigne > longueur) {
                longueur = longueurLigne;
            }
        }

        return longueur;
    }

    private void afficherCellule(Object cellule, int tailleMax) {
        int max = tailleMax > MAX_COL_LONG ? MAX_COL_LONG : tailleMax;
        max = max < MIN_COL_LONG ? MIN_COL_LONG : max;
        String celluleString;
        if (cellule == null) {
            celluleString = "null";
        } else if (cellule instanceof Boolean) {
            celluleString = (Boolean)cellule ? "vrai" : "faux";
        } else {
            celluleString = cellule.toString();
        }
        System.out.print(" ");
        if (celluleString.length() > max) {
            // azertyuiopqsdfghjklmnopqrstuvwxyz
            // 0123456789
            // max = 7
            // maxIndex = max - 3 = 2
            int maxIndex = max - 3; 
            System.out.print(celluleString.substring(0, maxIndex) + "...");
        } else {
            System.out.print(celluleString);
        }
        for (int i = 0; i < max - celluleString.length(); i++) {
            System.out.print(" ");
        }
        System.out.print(" |");
    }

    private void afficherSeparation(int[] maxColsLong) {
        System.out.print("+");
        for (int i : maxColsLong) {
            int max = i > MAX_COL_LONG ? MAX_COL_LONG : i;
            max = max < MIN_COL_LONG ? MIN_COL_LONG : max;
            for (int j = 0; j < max + 2; j++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

}