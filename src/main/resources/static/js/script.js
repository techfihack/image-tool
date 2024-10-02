document.addEventListener('DOMContentLoaded', function() {
    // Document is ready

    // Add an event listener to the range input
    document.getElementById('compressQuality').addEventListener('input', function() {
        // Update the value in the span element
        document.getElementById('rangeValue').innerText = this.value + "%";
    });

    // Fetch data from /api/image/format using the Fetch API
    fetch('/api/image/formats')
        .then(response => response.json())
        .then(data => {
            // Populate the select element with options
            const selectElement = document.getElementById('imageFormats');

            // Clear existing options
            selectElement.innerHTML = '';

            // Append options from the fetched data
            data.forEach(format => {
                const option = document.createElement('option');
                option.value = format;
                option.text = format.toUpperCase();
                selectElement.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching data:', error));

    // Add these variables at the top of your script
    let applyToAllCheckbox = document.getElementById('applyToAll');
    let globalWidthInput = document.getElementById('globalWidth');
    let globalHeightInput = document.getElementById('globalHeight');

    // Add this function to handle checkbox change
    applyToAllCheckbox.addEventListener('change', function() {
        globalWidthInput.style.display = this.checked ? 'inline-block' : 'none';
        globalHeightInput.style.display = this.checked ? 'inline-block' : 'none';
        if (!this.checked) {
            globalWidthInput.value = '';
            globalHeightInput.value = '';
            // Reset all width and height inputs to their original values
            resetDimensions();
        }
    });

    // Add this function to handle global width input change
    globalWidthInput.addEventListener('input', function() {
        if (applyToAllCheckbox.checked) {
            let newWidth = parseInt(this.value);
            if (isNaN(newWidth) || newWidth <= 0) return;
            updateAllDimensions(newWidth, null);
        }
    });

    // Function to update all dimensions
    function updateAllDimensions(newWidth, newHeight) {
        const widthInputs = document.getElementsByName('width[]');
        const heightInputs = document.querySelectorAll('[id^="height"]');

        for (let i = 0; i < widthInputs.length; i++) {
            const aspectRatio = parseFloat(widthInputs[i].dataset.aspectRatio);
            if (newWidth) {
                widthInputs[i].value = newWidth;
                heightInputs[i].value = Math.round(newWidth / aspectRatio);
            } else if (newHeight) {
                heightInputs[i].value = newHeight;
                widthInputs[i].value = Math.round(newHeight * aspectRatio);
            }
        }
    }

    // Function to reset dimensions to original values
    function resetDimensions() {
        const widthInputs = document.getElementsByName('width[]');
        const heightInputs = document.querySelectorAll('[id^="height"]');

        for (let i = 0; i < widthInputs.length; i++) {
            const originalWidth = widthInputs[i].placeholder.split(' ')[0];
            widthInputs[i].value = originalWidth;
            const aspectRatio = parseFloat(widthInputs[i].dataset.aspectRatio);
            heightInputs[i].value = Math.round(originalWidth / aspectRatio);
        }
    }

    // Function to update the file list and preview images
    function updateFileList() {
        const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
        fileList.innerHTML = ''; // Clear existing rows

        const files = document.getElementById('files').files;
        console.log("Files selected:", files); // Debugging log
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const row = fileList.insertRow();

            // Filename column with preview
            const filenameCell = row.insertCell(0);
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.style.width = '50px';
            img.style.height = '50px';
            img.style.marginRight = '10px';
            filenameCell.appendChild(img);
            filenameCell.appendChild(document.createTextNode(file.name));

            // Resized (width in pixel) column
            const widthCell = row.insertCell(1);
            const widthInput = document.createElement('input');
            widthInput.type = 'number';
            widthInput.className = 'form-control';
            widthInput.name = `width_${i}`;
            widthInput.style.width = '100px';
            widthCell.appendChild(widthInput);

            // Resized (height in pixel) column
            const heightCell = row.insertCell(2);
            const heightInput = document.createElement('input');
            heightInput.type = 'number';
            heightInput.className = 'form-control';
            heightInput.name = `height_${i}`;
            heightInput.style.width = '100px';
            heightCell.appendChild(heightInput);

            // Read image dimensions
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = new Image();
                img.onload = function() {
                    console.log("Image dimensions:", img.width, img.height); // Debugging log
                    widthInput.value = img.width;
                    heightInput.value = img.height;
                };
                img.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }

    // Attach the updateFileList function to the file input change event
    document.getElementById('files').addEventListener('change', updateFileList);

    // Attach the convertImage function to the button click event
    document.getElementById('btn-convert-image').addEventListener('click', convertImage);
});

function convertImage() {
    // Your existing convertImage function code
}

function singleConversion(formData) {
    // Your existing singleConversion function code
}

function multiConversion(formData) {
    // Your existing multiConversion function code
}