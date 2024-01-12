package relation.operation;

import java.util.Vector;

import relation.Relation;
import relation.syntaxe.Syntaxe;

public class NoeudOperation {
    
    private String valeur, valeurInitiale;
    private Vector<NoeudOperation> enfants = new Vector<NoeudOperation>();
    private Vector<Object> ligne;
    private Vector<String> colonnes, domaines;
    private static String[] operateurs = {"+", "-", "*",  "/", "%"};

    public NoeudOperation(String valeur, Vector<Object> ligne, Vector<String> colonnes, Vector<String> domaines) {
        this.valeur = valeurInitiale = valeur;
        this.ligne = ligne;
        this.colonnes = colonnes;
        this.domaines = domaines;
    }

    public String getValeurInitiale() {
        return valeurInitiale;
    }

    private String getOperateur() throws Exception {
        for (String operateur : operateurs) {
            if (Syntaxe.contientHGHC(valeur, operateur)) {
                return operateur;
            }
        }
        return "";
    }

    public void arranger() throws Exception {
        String operateur = getOperateur();

        if (!operateur.equals("")) {
            String regexOperateur = 
                (
                    operateur.equals("+") ||
                    operateur.equals("*")
                ) ? "\\" + operateur : operateur;
            for (String partie : Syntaxe.separerHGHC(valeur, regexOperateur)) {
                if (!partie.equals("")) {
                    enfants.add(new NoeudOperation(partie.trim(), ligne, colonnes, domaines));
                }
            }
            valeur = operateur;
        } else if (Syntaxe.compterCaractereHG(valeur, '[') == 1 && Syntaxe.compterCaractere(valeur, ']') == 1) {
            valeur = valeur.trim().substring(1, valeur.length() - 1).trim();
            arranger();
        } else if (Syntaxe.contientHG(valeur, "[") && Syntaxe.contientHG(valeur, "]")) {
            throw new Exception("Un opérateur logique tel que \" OU \" ou \" ET \" doit se trouver entre 2 groupes de prédicats comme ceci : [prédicats] OU [prédicats]");
        }

        for (NoeudOperation noeudPredicat : enfants) {
            noeudPredicat.arranger();
        }

    }

    public Vector<NoeudOperation> getEnfants() {
        return enfants;
    }

    public void setEnfants(Vector<NoeudOperation> enfants) {
        this.enfants = enfants;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public String getValeurExpression() throws Exception {
        String resultat = "";

        switch (valeur) {
            case "+":
                if (enfants.size() == 0) {
                    throw new Exception("L'opération \"+\" doit être au moins suivi par une expression");
                } else {
                    String type = enfants.get(0).getType();
                    if (type.equals("entier") || type.equals("decimal")) {
                        double temp = 0.0;
                        for (NoeudOperation noeudOperation : enfants) {
                            try {
                                double valeurEnfant = Double.parseDouble(noeudOperation.getValeurExpression());
                                temp += valeurEnfant;
                            } catch (Exception e) {
                                throw new Exception("Erreur de syntaxe près de " + noeudOperation.getValeurInitiale());
                            }
                        }
                        resultat = String.valueOf(temp);
                    } else if (type.equals("lettres")) {
                        for (NoeudOperation noeudOperation : enfants) {
                            if (noeudOperation.getType().equals(type)) {
                                String valeurExpression = noeudOperation.getValeurExpression();
                                resultat += valeurExpression.substring(1, valeurExpression.length() - 1);
                            } else {
                                throw new Exception("Erreur de syntaxe près de " + noeudOperation.getValeurInitiale());
                            }
                        }
                        resultat = "\"" + resultat + "\"";
                    } else {
                        throw new Exception("Erreur de syntaxe près de " + enfants.get(0).getValeurInitiale());
                    }
                }
                break;
        
            case "-":
                if (enfants.size() == 0) {
                    throw new Exception("L'opération \"-\" doit être au moins suivi par une expression");
                } else if (enfants.size() == 1) {
                    try {
                        double valeurEnfant = Double.parseDouble(enfants.get(0).getValeurExpression());
                        resultat = String.valueOf(-valeurEnfant);
                    } catch (Exception e) {
                        throw new Exception("Erreur de syntaxe près de " + enfants.get(0).getValeurInitiale());
                    }
                } else {
                    double temp = 0.0;
                    for (int i = 0; i < enfants.size(); i++) {
                        NoeudOperation enfant = enfants.get(i);
                        try {
                            double valeurEnfant = Double.parseDouble(enfant.getValeurExpression());
                            if (i == 0) {
                                temp = valeurEnfant;
                            } else {
                                temp -= valeurEnfant;
                            }
                        } catch (Exception e) {
                            throw new Exception("Erreur de syntaxe près de " + enfant.getValeurInitiale());
                        }
                    }
                    resultat = String.valueOf(temp);
                }
                break;
        
            case "*":
                if (enfants.size() < 2) {
                    throw new Exception("L'opération \"*\" doit être par 2 expressions");
                } else {
                    double temp = 1.0;
                    for (NoeudOperation noeudOperation : enfants) {
                        try {
                            double valeurEnfant = Double.parseDouble(noeudOperation.getValeurExpression());
                            temp *= valeurEnfant;
                        } catch (Exception e) {
                            throw new Exception("Erreur de syntaxe près de " + noeudOperation.getValeurInitiale());
                        }
                    }
                    resultat = String.valueOf(temp);
                }
                break;
        
            case "/":
                if (enfants.size() < 2) {
                    throw new Exception("L'opération \"/\" doit être par 2 expressions");
                } else {
                    double temp = 0.0;
                    for (int i = 0; i < enfants.size(); i++) {
                        NoeudOperation enfant = enfants.get(i);
                        try {
                            double valeurEnfant = Double.parseDouble(enfant.getValeurExpression());
                            if (i == 0) {
                                temp = valeurEnfant;
                            } else if(i % 2 == 0) {
                                temp *= valeurEnfant;
                            } else if (valeurEnfant != 0) {
                                temp /= valeurEnfant;
                            } else {
                                throw new Exception("Division par zéro impossible !");
                            }
                        } catch (Exception e) {
                            throw new Exception("Erreur de syntaxe près de " + enfant.getValeurInitiale());
                        }
                    }
                    resultat = String.valueOf(temp);
                }
                break;
        
            case "%":
                if (enfants.size() < 2) {
                    throw new Exception("L'opération \"%\" doit être par 2 expressions");
                } else {
                    double temp = 0.0;
                    for (int i = 0; i < enfants.size(); i++) {
                        NoeudOperation enfant = enfants.get(i);
                        try {
                            double valeurEnfant = Double.parseDouble(enfant.getValeurExpression());
                            if (i == 0) {
                                temp = valeurEnfant;
                            } else {
                                temp %= valeurEnfant;
                            }
                        } catch (Exception e) {
                            throw new Exception("Erreur de syntaxe près de " + enfant.getValeurInitiale());
                        }
                    }
                    resultat = String.valueOf(temp);
                }
                break;
        
            default:
                String type = Relation.getTypeTerme(valeur);
                if (type.equals("colonne")) {
                    resultat = String.valueOf(ligne.get(colonnes.indexOf(valeur)));
                } else {
                    resultat = valeur;
                }
                break;
        }

        return resultat;
    }

    public String getType() throws Exception {
        String valeurExpression = getValeurExpression(),
            type = Relation.getTypeTerme(valeurExpression);
        if (type.equals("colonne")) {
            if (!colonnes.contains(valeurExpression)) {
                throw new IllegalArgumentException("Colonne " + valeurExpression + " inconnue");
            } else {
                type = domaines.get(colonnes.indexOf(valeurExpression));
            }
        }

        return type;
    }

}
