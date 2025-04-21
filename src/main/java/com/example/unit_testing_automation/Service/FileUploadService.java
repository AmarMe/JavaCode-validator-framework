package com.example.unit_testing_automation.Service;

import com.example.unit_testing_automation.Model.TestFile;
import com.example.unit_testing_automation.Repository.FileUploadRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileUploadService {

    @Autowired
    private FileUploadRepository repository;

//    private static final String UPLOAD_DIR = "C:\\Users\\DELL\\Documents\\Practice\\BNY-Test-Automation-tool\\unit-testing-automation\\unit-testing-automation\\src\\main\\resources\\uploads\\";

    public ResponseEntity<String> uploadModule(MultipartFile file) throws IOException {
        List<String> methodsList = new ArrayList<>();

        String originalUploadedFileName = file.getOriginalFilename();
        Path tempDir = Files.createTempDirectory("compile_dir");
        System.out.println("Temp File Directory: "+tempDir);
        File tempJavaFile = new File(tempDir.toFile(), originalUploadedFileName);
        file.transferTo(tempJavaFile);
        tempJavaFile.deleteOnExit();

        String result = executeTests(tempJavaFile,originalUploadedFileName);

//        JavaParser parser = new JavaParser();
//        ParseResult<CompilationUnit> parseResult = parser.parse(tempJavaFile);
//        if(parseResult.isSuccessful() && parseResult.getResult().isPresent()){
//            CompilationUnit compilationUnit = parseResult.getResult().get();
//            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
//
//            if(!classes.isEmpty()){
//                for(ClassOrInterfaceDeclaration className: classes){
//                    String clazzName = className.getNameAsString();
//                    System.out.println("Class file name: "+clazzName);
//
//                    List<MethodDeclaration> methods = className.getMethods();
//                    for(MethodDeclaration method: methods){
//                        String methodName = method.getNameAsString();
//                        methodsList.add(methodName);
//                        String testStatus = executeTests(tempJavaFile,originalUploadedFileName);
//                        TestFile testFile = new TestFile(clazzName,methodName,testStatus);
//                        repository.save(testFile);
//                    }
//                    System.out.println("Method names list: "+methodsList);
//                }
//            }
//        }
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    public String executeTests(File tempJavaFile, String fileName){
        try{
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int compiledResult = compiler.run(null,null,null, tempJavaFile.getPath());
            File tempParentDir = tempJavaFile.getParentFile();
            if(compiledResult!=0)
                return "Error occurred while compiling java class file";

            String javaFileName = fileName.replace(".java","");
            String className = "com.example.unit_testing_automation.TestDataFile."+javaFileName;

            URLClassLoader classLoader= URLClassLoader.newInstance(new URL[]{tempParentDir.toURI().toURL()});
            Class<?> clazz = Class.forName(className,true,classLoader);
            Object instance = clazz.getDeclaredConstructor().newInstance();

//            Method methodRetrieved = clazz.getDeclaredMethod("addition");
//            System.out.println("The Method name is: "+methodRetrieved.getName());

            for(Method method: clazz.getDeclaredMethods()){
                if(method.getParameterCount()==0){
                    method.setAccessible(true);
                    Object methodResult =  method.invoke(instance);
                    String actualOutput = (methodResult!=null)? methodResult.toString() : "null";
                    String expectedOutput = getMethodExpectedOutput(method.getName());
                    String testStatus = (actualOutput.equals(expectedOutput))? "Passed" : "Failed";
                    String methodName = method.getName();
                    TestFile testFile = new TestFile(javaFileName,methodName,testStatus);
                    repository.save(testFile);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while running tests: "+e.getMessage());
        }
        return "File uploaded and tested successfully";
    }

    private String getMethodExpectedOutput(String methodName) {
        String expectedOutput = switch (methodName) {
            case "testingSimulation" -> "method testingSimulation tested successfully";
            case "addition" -> "Answer is: 20";
            case "multiplication" -> "Answer is: 110";
            default -> "";
        };
        return expectedOutput;
    }


    // Get All
    public List<TestFile> allTestReports() {
        List<TestFile> testFileList = repository.findAll();
        return testFileList;
    }

    //excel generator
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<TestFile> testFile = repository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("TestsReport");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Class_Name");
        headerRow.createCell(2).setCellValue("Method_Name");
        headerRow.createCell(3).setCellValue("Test_Status");

        int rowNum = 1;
        for (TestFile tfs : testFile) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tfs.getId());
            row.createCell(1).setCellValue(tfs.getClassName());
            row.createCell(2).setCellValue(tfs.getMethodName());
            row.createCell(3).setCellValue(tfs.getTestStatus());

        }
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Tests_report.xlsx");

        try {
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook.close();
    }
    public List<Map<String, Object>> readExcelSimple(MultipartFile file) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();

        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        int totalCols = headerRow.getLastCellNum();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, Object> rowData = new LinkedHashMap<>();

            for (int j = 0; j < totalCols; j++) {
                String header = headerRow.getCell(j).getStringCellValue().trim();
                String cellValue = row.getCell(j) != null ? row.getCell(j).toString().trim() : "";

                if (header.equalsIgnoreCase("input1")) {
                    // Use manual splitting and processing instead of streams
                    String[] rawInputs = cellValue.split(",");
                    List<Object> inputs = new ArrayList<>();

                    for (int k = 0; k < rawInputs.length; k++) {
                        String value = rawInputs[k].trim();                      // Step 1: Trim whitespace
                        Object converted = convertToBestType(value);            // Step 2: Convert to best type
                        inputs.add(converted);                                  // Step 3: Add to list
                    }
                    System.out.println(inputs);
                    rowData.put(header, inputs);                                // Step 4: Store in map
                } else {
                    rowData.put(header, cellValue);
                }
            }
            System.out.println(rowData);
            result.add(rowData);
        }
        System.out.println(result);
        workbook.close();
        return result;
    }

    private Object convertToBestType(String value) {
        // Check for Integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}


        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}

        // Check for Char (only one character)
        if (value.length() == 1) {
            return value.charAt(0);
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        // Default to String
        return value;
    }



    public ResponseEntity<String> uploadAndRunTest(MultipartFile javaFile, MultipartFile testcaseExcelFile) {
        String result = "";
        try {
            String originalUploadedFileName = javaFile.getOriginalFilename();
            Path tempDir = Files.createTempDirectory("JavaFile_compile_dir");
            System.out.println("Temp File Directory: "+tempDir);
            File tempJavaFile = new File(tempDir.toFile(), originalUploadedFileName);
            javaFile.transferTo(tempJavaFile);
            tempJavaFile.deleteOnExit();

            result = extractTestcaseExcelInputAndExecuteTests(tempJavaFile,originalUploadedFileName,testcaseExcelFile);

        } catch (Exception e) {
            System.out.println("Exception while processing file: "+e.getMessage());
            return new ResponseEntity<>("Something went wrong while processing file",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    private String extractTestcaseExcelInputAndExecuteTests(File tempJavaFile, String fileName,MultipartFile testcaseExcelFile ){
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int compiledResult = compiler.run(null,null,null, tempJavaFile.getPath());
            File tempParentDir = tempJavaFile.getParentFile();
            if(compiledResult!=0)
                return "Error occurred while compiling java class file";

            String javaFileName = fileName.replace(".java","");
            String className = "com.example.unit_testing_automation.TestDataFile."+javaFileName;
            URLClassLoader classLoader= URLClassLoader.newInstance(new URL[]{tempParentDir.toURI().toURL()});
            Class<?> clazz = Class.forName(className,true,classLoader);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            List<Map<String,Object>> testCaseList = extractExcelTestCaseData(testcaseExcelFile);
            for(Map<String,Object> testcase : testCaseList){
                String methodName = testcase.get("methodName").toString();
                Object[] inputValues = testcase.get("inputs");
                String expectedOutput = testcase.get("expectedOutput").toString();

                Object[] inputValues = (Object[]) inputs;
            }


        } catch (Exception e) {
            System.out.println("Exception while running test: "+e.getMessage());
        }
        return "File uploaded and tested successfully";
    }

    private List<Map<String,Object>> extractExcelTestCaseData(MultipartFile testcaseExcelFile) {
        List<Map<String,Object>> testcasesList = new ArrayList<>();

        return testcasesList;

    }
}
