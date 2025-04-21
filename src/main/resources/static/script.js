
document.getElementById('fileInput').addEventListener('change', function () {
    const fileName = this.files[0] ? this.files[0].name : 'No file selected';
    document.getElementById('selectedFile').textContent = fileName;
});

document.getElementById('excelInput').addEventListener('change', function () {
    const fileName = this.files[0] ? this.files[0].name : 'No file selected';
    document.getElementById('selectedExcelFile').textContent = fileName;
});


    document.getElementById('fileInput').addEventListener('change', function () {
        const fileName = this.files[0] ? this.files[0].name : 'No file selected';
        document.getElementById('selectedFile').textContent = fileName;
    });
});


// Upload file to backend and handle responses
function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    document.getElementById('uploadSuccess').style.display = 'none';
    document.getElementById('uploadError').style.display = 'none';

    if (!file) {
        document.getElementById('uploadError').textContent = "Please choose a file to upload.";
        document.getElementById('uploadError').style.display = 'block';
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch("http://localhost:8080/unit-test-api/v1/upload-file", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text);
            });
        }
        return response.text(); // Get plain text (just like Postman)
    })
    .then(message => {
        // Display success message in regular alert
        document.getElementById('uploadSuccess').textContent = message;
        document.getElementById('uploadSuccess').style.display = 'block';

        // Display buttons
        document.getElementById("viewReportsBtn").style.display = "inline-block";
        document.getElementById("downloadReportBtn").style.display = "inline-block";

        // Show popup with the specific message including method list
        document.getElementById('popupMessage').textContent = "File uploaded and tested successfully: Method list[main]";
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