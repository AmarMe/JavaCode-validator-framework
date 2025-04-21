
document.getElementById('fileInput').addEventListener('change', function () {
    const fileName = this.files[0] ? this.files[0].name : 'No file selected';
    document.getElementById('selectedFile').textContent = fileName;
});

document.getElementById('excelInput').addEventListener('change', function () {
    const fileName = this.files[0] ? this.files[0].name : 'No file selected';
    document.getElementById('selectedExcelFile').textContent = fileName;
});


function uploadFile() {
    const javaInput = document.getElementById('fileInput');
    const excelInput = document.getElementById('excelInput');

    const javaFile = javaInput.files[0];
    const excelFile = excelInput.files[0];

    document.getElementById('uploadSuccess').style.display = 'none';
    document.getElementById('uploadError').style.display = 'none';

    if (!javaFile || !excelFile) {
        document.getElementById('uploadError').textContent = "Please choose both Java and Excel files.";
        document.getElementById('uploadError').style.display = 'block';
        return;
    }

    const formData = new FormData();
    formData.append("javaFile", javaFile);
    formData.append("excelFile", excelFile);

    fetch("http://localhost:8080/unit-test-api/v1/upload-both", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.text();
    })
    .then(message => {
        document.getElementById('uploadSuccess').textContent = message;
        document.getElementById('uploadSuccess').style.display = 'block';
        document.getElementById("viewReportsBtn").style.display = "inline-block";
        document.getElementById("downloadReportBtn").style.display = "inline-block";
        document.getElementById('popupMessage').textContent = "Files uploaded and tested successfully.";
        document.getElementById('successPopup').style.display = 'flex';
    })
    .catch(error => {
        document.getElementById('uploadError').textContent = "Upload failed: " + error.message;
        document.getElementById('uploadError').style.display = 'block';
    });
}


// Fetch and display employee test report from backend
function loadEmployees() {
    fetch("http://localhost:8080/unit-test-api/v1/test-reports")
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById("reportBody");
            tbody.innerHTML = "";

            // Display each employee's test report in table rows
            data.forEach(emp => {
                let statusClass = '';
                if (emp.testStatus === 'PASSED') {
                    statusClass = 'status-passed';
                } else if (emp.testStatus === 'FAILED') {
                    statusClass = 'status-failed';
                } else if (emp.testStatus === 'SKIPPED') {
                    statusClass = 'status-skipped';
                }

                const row = `
                    <tr>
                        <td>${emp.className}</td>
                        <td>${emp.methodName}</td>
                        <td class="${statusClass}">${emp.testStatus}</td>
                    </tr>
                `;

                tbody.innerHTML += row;
            });

            // Show the table after data is loaded
            document.getElementById("reportTable").style.display = "table";
        })
        .catch(error => {
            document.getElementById('uploadError').textContent = "Error loading test report: " + error.message;
            document.getElementById('uploadError').style.display = 'block';
        });
}

// Export the test report as Excel file
function downloadReport() {
    fetch('http://localhost:8080/unit-test-api/v1/export-data', {
        method: 'GET'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to export report');
        }
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'test_report.xlsx');
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    })
    .catch(error => {
        console.error('Error downloading the report:', error);
    });
}

// Function to close popup
function closePopup() {
    document.getElementById('successPopup').style.display = 'none';
}