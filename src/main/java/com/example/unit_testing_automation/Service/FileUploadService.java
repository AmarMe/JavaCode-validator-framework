package com.example.unit_testing_automation.Service;

import com.example.unit_testing_automation.Model.TestFile;
import com.example.unit_testing_automation.Repository.FileUploadRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
        XSSFSheet sheet = workbook.createSheet("JavaFile test report");

        // Create styles
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        XSSFFont normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 12);

        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(customizedColumnColor(120, 162, 222));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setAlignment(HorizontalAlignment.CENTER);
        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        XSSFCellStyle passStyle = getXssfCellStyle(214, 247, 208,workbook, borderStyle);
        XSSFCellStyle failStyle = getXssfCellStyle(252, 197, 197,workbook, borderStyle);

        // Create Header
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(40);
        String[] headers = {"ID", "Class_Name", "Method_Name", "Test_Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add Data
        int rowNum = 1;
        for (TestFile tfs : testFile) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(25);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(tfs.getId());
            cell0.setCellStyle(borderStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(tfs.getClassName());
            cell1.setCellStyle(borderStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(tfs.getMethodName());
            cell2.setCellStyle(borderStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(tfs.getTestStatus());
            if ("Passed".equalsIgnoreCase(tfs.getTestStatus())) {
                cell3.setCellStyle(passStyle);
            } else if ("Failed".equalsIgnoreCase(tfs.getTestStatus())) {
                cell3.setCellStyle(failStyle);
            } else {
                cell3.setCellStyle(borderStyle); // Default style
            }
        }

        for (int i = 0; i < 4; i++) {
//            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i,6000);

        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=file.xlsx");

        try {
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook.close();
    }

    private XSSFCellStyle getXssfCellStyle(int r, int g, int b,XSSFWorkbook workbook, CellStyle borderStyle) {
        XSSFCellStyle passStyle = workbook.createCellStyle();
        passStyle.cloneStyleFrom(borderStyle);
        passStyle.setFillForegroundColor(customizedColumnColor(r,g,b));
        passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        passStyle.setAlignment(HorizontalAlignment.CENTER);
        passStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return passStyle;
    }

    public XSSFColor customizedColumnColor(int r, int g, int b){
        Color rgb = new Color(r,g,b);
        return new XSSFColor(rgb,new DefaultIndexedColorMap());
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

                if (header.equalsIgnoreCase("Inputs")) {
                    // Use manual splitting and processing instead of streams
                    cellValue = cellValue.replaceAll("[\\[\\]]","");
                    String[] rawInputs = cellValue.split(",");
                    List<Object> inputs = new ArrayList<>();

                    for (int k = 0; k < rawInputs.length; k++) {
                        String value = rawInputs[k].trim();
                        Object converted = convertToBestType(value);
                        inputs.add(converted);
                    }
                    System.out.println(inputs);
                    rowData.put(header, inputs);
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
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        value = value.trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}

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

            List<Map<String,Object>> testCaseList = readExcelSimple(testcaseExcelFile);
            for(Map<String,Object> testcase : testCaseList){
                String methodName = testcase.get("Method Name").toString();
                List<?> inputValuesList = (List<?>) testcase.get("Inputs");
                Object[] inputValues = inputValuesList.toArray();

                String expectedOutput = testcase.get("Expected Output").toString();
                int expectedStatusCode = (int) Double.parseDouble(testcase.get("Expected Status Code").toString());
                for(Method method: clazz.getDeclaredMethods()){
                    if(!method.getName().equals(methodName)) continue;

                    Class<?>[] parameters = method.getParameterTypes();
                    if(inputValues.length != parameters.length) continue;

                    Object[] finalParameterValues = new Object[parameters.length];
                    for(int i=0;i< parameters.length;i++){
                        finalParameterValues[i] = convertToParamType(inputValues[i],parameters[i]);
                    }

                    Object actualResult = method.invoke(instance,finalParameterValues);
                    ResponseEntity<?> actualResponse = (ResponseEntity<?>) actualResult;
                    int actualStatusCode = actualResponse.getStatusCode().value();
                    String actualOutput = Objects.requireNonNull(actualResponse.getBody()).toString();
                    actualOutput = (actualOutput!=null)? actualOutput : "null";
                    String testStatus = (actualOutput.equals(expectedOutput)) && (actualStatusCode == expectedStatusCode)? "Passed" : "Failed";

                    TestFile testFile = new TestFile(javaFileName,methodName,testStatus);
                    repository.save(testFile);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while running test: "+e.getMessage());
        }
        return "File uploaded and tested successfully";
    }

    private Object convertToParamType(Object inputValue, Class<?> parameter) {
        if(inputValue==null) return null;
        if(parameter.isAssignableFrom(inputValue.getClass())){
            return inputValue;
        }
        String rawValue = inputValue.toString();
        if(parameter == int.class || parameter == Integer.class)
            return Integer.parseInt(rawValue);
        else if(parameter == double.class || parameter == Double.class)
            return Double.parseDouble(rawValue);
        else if(parameter == boolean.class || parameter == Boolean.class)
            return Boolean.parseBoolean(rawValue);
        else if(parameter == long.class || parameter == Long.class)
            return Long.parseLong(rawValue);
        else if(parameter == String.class)
            return rawValue;

        throw new IllegalArgumentException("Unsupported parameter type: "+parameter.getName());
    }
}
