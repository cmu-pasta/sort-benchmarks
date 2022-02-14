package diff;

import com.google.javascript.jscomp.FunctionInformationMap;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.VariableMap;

import java.io.Serializable;
import java.util.Set;

class NewResult implements Serializable {
    public final boolean success;
    public final JSError[] errors;
    public final JSError[] warnings;
    public final byte[] variableMap;
    public final byte[] propertyMap;
    public final byte[] namedAnonFunctionMap;
    public final byte[] stringMap;
    public final FunctionInformationMap functionInformationMap;
    //public final SourceMap sourceMap;
    //public final Map<String, Integer> cssNames;
    public final String externExport;
    public final String idGeneratorMap;
    public final Set<SourceFile> transpiledFiles;

    NewResult(boolean success, JSError[] errors, JSError[] warnings, VariableMap variableMap, VariableMap propertyMap, VariableMap namedAnonFunctionMap, VariableMap stringMap, FunctionInformationMap functionInformationMap, /*SourceMap sourceMap,*/ String externExport, /*Map<String, Integer> cssNames,*/ String idGeneratorMap, Set<SourceFile> transpiledFiles) {
        this.success = success;
        this.errors = errors;
        this.warnings = warnings;
        this.variableMap = variableMap.toBytes();
        this.propertyMap = propertyMap.toBytes();
        this.namedAnonFunctionMap = namedAnonFunctionMap.toBytes();
        this.stringMap = stringMap.toBytes();
        this.functionInformationMap = functionInformationMap;
        //this.sourceMap = sourceMap;
        this.externExport = externExport;
        //this.cssNames = cssNames;
        this.idGeneratorMap = idGeneratorMap;
        this.transpiledFiles = transpiledFiles;
    }
}
