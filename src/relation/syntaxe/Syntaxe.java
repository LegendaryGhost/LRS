package relation.syntaxe;

import java.util.Vector;
import java.util.regex.Pattern;

public class Syntaxe {

    private static String[] mots_cles = {
        "CHOISIR",
        "AJOUTER",
        "DANS",
        "CREER",
        "RELATION",
        "PREDICATS",
        "JOINTURE",
        "PRODUIT",
        "OU",
        "NON",
        "LIMITE",
        "UNION",
        "DIFFERENCE",
        "INTERSECTION"
    };

    private static String[] caracteres_spec = {
        "[", "]", ".", ";", " ", "/", "*", "%", "+", "-", "(", ")"
    };
    private static String[] operateurs = {"/", "*", "%", "+", "-"};

    public static boolean estMotCle(String chaine) {
        for (String mot_cle : mots_cles) {
            if (chaine.toUpperCase().equals(mot_cle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contientCarSpec(String chaine) {
        for (String car_spec : caracteres_spec) {
            if (chaine.contains(car_spec)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sépare les parties d'une chaîne entourées par des guillemets et celles qui ne le sont pas
     * "\"test\", test2 \"test3\"" => {
     *      "\"test\"",
     *      ", test2 \",
     *      "\"test3\""
     * }
     * @param texte
     * @return
     */
    private static Vector<String> obtenirParties(String texte) {
        Vector<String> parties = new Vector<String>();
        while (!texte.equals("")) {
            int indice_guillemet = texte.indexOf("\"");
            String partie = indice_guillemet != -1 ? texte.substring(0, indice_guillemet) : texte;
            if (!partie.equals("")) {
                parties.add(partie);
            }
            texte = indice_guillemet != -1 ? texte.substring(indice_guillemet) : "";
            if (!texte.equals("")) {
                indice_guillemet = texte.substring(1).indexOf("\"") + 1;
                partie = texte.substring(0, indice_guillemet + 1);
                if (!partie.equals("")) {
                    parties.add(partie);
                }
                texte = texte.substring(indice_guillemet + 1);
            }
        }
        return parties;
    }

    private static Vector<String> obtenirPartiesHG(String texte) {
        Vector<String> parites = obtenirParties(texte);
        Vector<String> resultat = new Vector<String>();
        for (int i = 0; i < parites.size(); i++) {
            if (!parites.get(i).startsWith("\"")) {
                resultat.add(parites.get(i));   
            }
        }
        return resultat;
    }

    public static Vector<String> obtenirPartiesParCrochet(String texte) {
        return obtenirPartiesParCar(texte, '[', ']');
    }

    public static Vector<String> obtenirPartiesParCar(String texte, char carOuvrant, char carFermant) {
        Vector<String> resultat = new Vector<String>();
        if (compterCaractereHG(texte, carOuvrant) != compterCaractereHG(texte, carFermant)) {
            throw new IllegalArgumentException("Il y a des caractères (" + carOuvrant + " ou " + carFermant +") en trop dans : " + texte);
        } else {
            String partie = "";
            while (!texte.equals("")) {
                int indiceCarO = indiceCarOHG(texte, carOuvrant);
                partie = texte.substring(0, indiceCarO != -1 ? indiceCarO : texte.length());
                if (!partie.trim().equals("")) {
                    resultat.add(partie);
                }
                texte = indiceCarO != -1 ? texte.substring(indiceCarO) : "";
                int indiceCarF = indiceCarFHG(texte, carOuvrant, carFermant);
                if (!texte.equals("")) {
                    partie = texte.substring(0, indiceCarF + 1);
                    if (!partie.trim().equals("")) {
                        resultat.add(partie);
                    }
                }
                texte = indiceCarF != -1 ? texte.substring(indiceCarF + 1) : "";
            }
        }

        return resultat;
    }

    public static Vector<String> obtenirPartiesHGHC(String texte) {
        return obtenirPartiesHGHCar(texte, '[', ']');
    }

    public static Vector<String> obtenirPartiesHGHCar(String texte, char carOuvrant, char carFermant) {
        Vector<String> paritesPCar = obtenirPartiesParCar(texte, carOuvrant, carFermant);
        Vector<String> resultat = new Vector<String>();
        for (String partiePCar : paritesPCar) {
            if (!partiePCar.startsWith(String.valueOf(carOuvrant))) {
                resultat.add(partiePCar);   
            }
        }
        return resultat;
    }

    public static Vector<String> obtenirPartiesHGDansC(String texte) {
        Vector<String> paritesPC = obtenirPartiesParCrochet(texte);
        Vector<String> resultat = new Vector<String>();
        for (int i = 0; i < paritesPC.size(); i++) {
            if (paritesPC.get(i).startsWith("[") && paritesPC.get(i).endsWith("]")) {
                resultat.add(paritesPC.get(i));   
            }
        }
        return resultat;
    }

    /**
     * Retourne l'indice du premier caractère ouvrant en dehors des guillemets
     * @param texte
     * @param carOuvrant
     * @return
     */
    public static int indiceCarOHG(String texte, char carOuvrant) {
        boolean ignore = false;
        for (int i = 0; i < texte.length(); i++) {
            if (texte.charAt(i) == '"') {
                ignore = !ignore;
            } else if (texte.charAt(i) == carOuvrant && !ignore) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retourne l'indice du caratère fermant le premier caractère ouvrant qu'on a rencontré hors des guillemets
     * Retourne l'indice de ")" qui fermet le premier "(" qu'on a rencontré hors des guillemets
     * Exemple : indiceCarFHG(
     *      "(1, \"2)\", 3)",
     *      "(",
     *      ")" 
     * ) => 11      Il s'agit de l'indice du dernier ")"
     * @param texte
     * @param carOuvrant
     * @param carFermant
     * @return
     */
    public static int indiceCarFHG(String texte, char carOuvrant, char carFermant) {
        boolean ignore = false,
            crochet_rencontre = false;
        int profondeur = 0;
        for (int i = 0; i < texte.length(); i++) {
            if (texte.charAt(i) == '"') {
                ignore = !ignore;
            } else if (texte.charAt(i) == carOuvrant && !ignore) {
                crochet_rencontre = true;
                profondeur++;
            } else if (texte.charAt(i) == carFermant && !ignore) {
                profondeur--;
            }
            if (crochet_rencontre && profondeur == 0) {
                return i;
            }
        }
        return -1;
    }

    public static String remplacerHG(String texte, String patterne, String remplacement) throws Exception {
        String resultat = "";

        if (!texte.contains("\"")) {
            resultat = texte.replace(patterne, remplacement);
        } else if(compterCaractere(texte, '"') % 2 == 1) {
            throw new Exception("Vous avez oublié de fermer une guillemet.");
        } else if (contientHG(texte, patterne)) {
            Vector<String> parties = obtenirParties(texte);
            for (String partie : parties) {
                if (partie.startsWith("\"")) {
                    resultat += partie;
                } else {
                    resultat += partie.replace(patterne, remplacement);
                }
            }
        } else {
            resultat = texte;
        }

        return resultat;
    }

    /**
     * Split hors guillemets tout en trimant chaque partie
     * @param texte
     * @param regex
     * @return
     * @throws Exception
     */
    public static Vector<String> separerHG(String texte, String regex) throws Exception {
        return separerHG(texte, regex, true);
    }
    
    /**
     * Fait un split insensible à la casse et qui ignore les caratères entre guillemets
     * @param texte
     * @param regex
     * @return
     * @throws Exception
     */
    public static Vector<String> separerHG(String texte, String regex, boolean trim) throws Exception {
        Vector<String> resultat = new Vector<String>();
        Pattern patterne = Pattern.compile(regex , Pattern.CASE_INSENSITIVE);

        if (!texte.contains("\"")) {
            // if (texte.toUpperCase().startsWith(regex)) {
            //     resultat.add("");
            // }
            for (String partie : patterne.split(texte)) {
                resultat.add(partie);
            }
            if (texte.toUpperCase().endsWith(regex)) {
                resultat.add("");
            }
        } else if(compterCaractere(texte, '"') % 2 == 1) {
            throw new Exception("Vous avez oublié de fermer une guillemet.");
        } else {
            Vector<String> parties = obtenirParties(texte);
            for (int i = 0; i < parties.size(); i++) {
                String partie = parties.get(i);
                if (partie.startsWith("\"")) {
                    if (i == 0) {
                        resultat.add(partie);
                    } else {
                        int dernier_indice = resultat.size() - 1;
                        resultat.set(dernier_indice,  resultat.get(dernier_indice) + partie);
                    }
                } else {
                    Vector<String> sous_parties = new Vector<String>();
                    // if (partie.toUpperCase().startsWith(regex)) {
                    //     sous_parties.add("");
                    // }
                    for (String sous_partie : patterne.split(partie)) {
                        sous_parties.add(sous_partie);
                    }
                    if (partie.toUpperCase().endsWith(regex)) {
                        sous_parties.add("");
                    }

                    if (sous_parties.size() == 0) {
                        sous_parties.add("");
                        sous_parties.add("");
                    }

                    if (i == 0) {
                        resultat.add(sous_parties.get(0));
                    } else {
                        int dernier_indice = resultat.size() - 1;
                        resultat.set(dernier_indice,  resultat.get(dernier_indice) + sous_parties.get(0));
                    }

                    for (int j = 1; j < sous_parties.size(); j++) {
                        resultat.add(sous_parties.get(j));
                    }
                }
            }
        }

        if (trim) {
            //Retirer les espaces en trop
            for (int i = 0; i < resultat.size(); i++) {
                resultat.set(i, resultat.get(i).trim());
            }
        }

        return resultat;
    }

    public static Vector<String> separerHGHC(String texte, String regex) throws Exception {
        return separerHGHC(texte, regex, true);
    }
    
    /**
     * Fait un split insensible à la casse et qui ignore les caratères entre guillemets et entre crochets
     * @param texte
     * @param regex
     * @return
     * @throws Exception
     */
    public static Vector<String> separerHGHC(String texte, String regex, boolean trim) throws Exception {
        return separerHGHCar(texte, regex, '[', ']', trim);
    }

    public static Vector<String> separerHGHP(String texte, String regex) throws Exception {
        return separerHGHP(texte, regex, true);
    }

    public static Vector<String> separerHGHP(String texte, String regex, boolean trim) throws Exception {
        return separerHGHCar(texte, regex, '(', ')', trim);
    }

    public static Vector<String> separerHGHCar(String texte, String regex, char carOuvrant, char carFermant, boolean trim) throws Exception {
        Vector<String> resultat = new Vector<String>();

        if (!contientHG(texte, String.valueOf(carOuvrant)) && !contientHG(texte, String.valueOf(carFermant))) {
            for (String partie : separerHG(texte, regex)) {
                resultat.add(partie);
            }
        } else if (compterCaractereHG(texte, carOuvrant) != compterCaractereHG(texte, carFermant)) {
            throw new IllegalArgumentException("Il y a des caractères (" + carOuvrant + " ou " + carFermant +") en trop dans : " + texte);
        } else {
            Vector<String> parties = obtenirPartiesParCar(texte, carOuvrant, carFermant);
            for (int i = 0; i < parties.size(); i++) {
                String partie = parties.get(i);
                if (partie.startsWith(String.valueOf(carOuvrant)) && partie.endsWith(String.valueOf(carFermant))) {
                    if (i == 0) {
                        resultat.add(partie);
                    } else {
                        int dernierIndice = resultat.size() - 1;
                        resultat.set(dernierIndice,  resultat.get(dernierIndice) + partie);
                    }
                } else {
                    Vector<String> sousParties = new Vector<String>();
                    // if (partie.toUpperCase().startsWith(regex)) {
                    //     sous_parties.add("");
                    // }
                    for (String sousPartie : separerHG(partie, regex, false)) {
                        sousParties.add(sousPartie);
                    }
                    // if (partie.toUpperCase().endsWith(regex)) {
                    //     sous_parties.add("");
                    // }
                    
                    if (
                        (sousParties.size() == 0) ||
                        (sousParties.size() == 1 && sousParties.get(0).equals(""))
                    ) {
                        sousParties = new Vector<String>();
                        sousParties.add("");
                        sousParties.add("");
                    }

                    if (i == 0) {
                        resultat.add(sousParties.get(0));
                    } else {
                        int dernierIndice = resultat.size() - 1;
                        resultat.set(dernierIndice,  resultat.get(dernierIndice) + sousParties.get(0));
                    }

                    for (int j = 1; j < sousParties.size(); j++) {
                        resultat.add(sousParties.get(j));
                    }
                }
            }
        }

        //Retirer les espaces en trop
        if (trim) {
            //Retirer les espaces en trop
            for (int i = 0; i < resultat.size(); i++) {
                resultat.set(i, resultat.get(i).trim());
            }
        }

        return resultat;
    }

    public static boolean contientHG(String texte, String patterne) throws Exception {
        if (!texte.contains("\"")) {
            return texte.contains(patterne);
        } else if(compterCaractere(texte, '"') % 2 == 1) {
            throw new Exception("Vous avez oublié de fermer une guillemet.");
        } else {
            // Les parties de la chaîne "texte" qui ne sont pas entourées par des guillemets
            Vector<String> parties_valides = obtenirPartiesHG(texte);
            for (String partie : parties_valides) {
                if (partie.contains(patterne)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public static boolean contientHGHP(String texte, String patterne) throws Exception {
        return contientHGHCar(texte, patterne, '(', ')');
    }

    public static boolean contientHGHC(String texte, String patterne) throws Exception {
        return contientHGHCar(texte, patterne, '[', ']');
    }

    public static boolean contientHGHCar(String texte, String patterne, char carOuvrant, char carFermant) throws Exception {
        if (!texte.contains(String.valueOf(carOuvrant))) {
            return contientHG(texte, patterne);
        } else if (compterCaractereHG(texte, carOuvrant) != compterCaractereHG(texte, carFermant)) {
            throw new IllegalArgumentException("Il y a des caractères( \"" + carOuvrant + "\" ou \"" + carFermant + "\" ) en trop dans " + texte);
        } else {
            // Les parties de la chaîne "texte" qui ne sont pas entourées par des caractères d'ouverture et de fermeture
            Vector<String> parties_valides = obtenirPartiesHGHCar(texte, carOuvrant, carFermant);
            for (String partie : parties_valides) {
                if (partie.contains(patterne)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public static int compterCaractere(String texte, char caractere) {
        int nbr_caracteres = 0;
        for (int i = 0; i < texte.length(); i++) {
            if (texte.charAt(i) == caractere) {
                nbr_caracteres++;
            }
        }
        return nbr_caracteres;
    }

    public static int compterCaractereHG(String texte, char caractere) {
        int nbr_caracteres = 0;
        for (String partieHG : obtenirPartiesHG(texte)) {
            nbr_caracteres += compterCaractere(partieHG, caractere);
        }
        return nbr_caracteres;
    }

    public static boolean contientOperateur(String terme) {
        for (String operateur : operateurs) {
            if (terme.contains(operateur)) {
                return true;
            }
        }
        return false;
    }

    // Vérifier si les parenthèses en dehors des guillemets sans correctements écrites
    public static boolean verifierParenthesesHG(String texte) {
        int profondeur = 0;
        boolean ignore = false;
        for (int i = 0; i < texte.length(); i++) {
            char car = texte.charAt(i);
            if (car == '"') {
                ignore = !ignore;
            } else if (car == '(' && !ignore) {
                profondeur++;
            } else if (car == ')' && !ignore) {
                profondeur--;
            }

            if (profondeur < 0) {
                throw new IllegalArgumentException("Il y a une parenthèse fermante en trop près de " + texte.substring(i));
            }
        }
        
        if (profondeur != 0) {
            throw new IllegalArgumentException("Il y a une parenthèse ouvrante en trop près de " + texte);
        }

        return true;
    }

}
