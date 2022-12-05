package substbma.evolution.alignment;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.datatype.DataType;
import substbma.evolution.datatype.LanguageIntegerData;

/**
 * @author Chieh-Hsi Wu
 */
@Description("These are the entity that is compared across lineages.")
public class Cognate extends BEASTObject {

    public Input<String> wordCodeInput = new Input<String>("words","Words coded as digits separated by commas", Input.Validate.REQUIRED);

    private String[] words;
    public void initAndValidate(){
        words = wordCodeInput.get().split("\\s+");
    }

    public int getWordCount(){
        return words.length;
    }

    public int[] getWords(DataType dataType){
        if(!(dataType instanceof LanguageIntegerData)){
            throw new RuntimeException("Require LanguageIntegerData.");
        }
        int[] wordCodes = new int[words.length];
        for(int i = 0; i < wordCodes.length; i++){
            wordCodes[i] = ((LanguageIntegerData)dataType).string2code(words[i]);
        }
        return wordCodes;

    }





}
