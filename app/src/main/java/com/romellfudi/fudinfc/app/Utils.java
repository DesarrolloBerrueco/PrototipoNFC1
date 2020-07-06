package com.romellfudi.fudinfc.app;

class Utils {

    public static boolean isValidDni(String dni){
        char[] dniLetters = {
                'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D',  'X',  'B', 'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'
        };
        dni = dni.trim();
        StringBuffer num = new StringBuffer();

        if(dni.length() < 8){
            return false;
        }

        //There are some dni with 8 characters
        if(dni.length() == 8) {
            dni = "0" + dni;
        }

        if (!Character.isLetter(dni.charAt(8))) {
            return false;
        }

        if (dni.length() != 9){
            return false;
        }

        for (int i=0; i<8; i++) {
            if(!Character.isDigit(dni.charAt(i))){
                if(i == 0){ //If it's a NIE, the first digit is a letter we have to manage it
                    char firstLetterNIE = dni.charAt(i);
                    int aux;
                    switch (firstLetterNIE){
                        case 'X':
                            aux = 0;
                            break;

                        case 'Y':
                            aux = 1;
                            break;

                        case 'Z':
                            aux = 2;
                            break;

                        default:
                            aux = 9;
                            break;
                    }

                    num.append(aux);
                }else{
                    return false;
                }
            }else{
                num.append(dni.charAt(i));
            }
        }

        int ind = Integer.parseInt(num.toString());

        // Calculate letter position on the char array
        ind %= 23;

        dni = num.toString() + Character.toUpperCase(dni.charAt(8));
        // Verify dni letter matches with expected letter from array
        if (dni.charAt(8) != dniLetters[ind]){
            return false;
        }

        return true;
    }
}
