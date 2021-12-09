import { Component, OnInit, Type } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators, FormArray} from '@angular/forms';
import { ApiService } from '../api.service';
import { ITypes } from '../ITypes';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  types: ITypes;
  objCSStats : any;
  capabilityStmtStats : any;
  igComparison: any;

  stats_server_ig1_fhirversion: string;
  stats_server_ig1_format: string;
  stats_server_ig1_version: string;

  stats_server_ig2_fhirversion: string;
  stats_server_ig2_format: string;
  stats_server_ig2_version: string;

  stats_client_ig1_fhirversion: string;
  stats_client_ig1_format: string;
  stats_client_ig1_version: string;

  stats_client_ig2_fhirversion: string;
  stats_client_ig2_format: string;
  stats_client_ig2_version: string;

  form: FormGroup;

  ig1List: Array<string>[];
  ig2List: Array<string>[];
  status: string;
  download_url: string;
  identifier: string;
  accumulatedSdTypes: string[];
  similarities : string[];
  multipleProfiles : string[] = [];
  singleProfile : string[] = [];
  conflicts: any;
  cs_conflicts: any;

  selected_similarities : string[];
  selected_conflicts: any;
  selected_singleProfile : string[];
  selected_multipleProfiles : string[];
  selected_cs: string[];

  csTypes : any;
  sdTypes: any;
  
  getTypesOutput: boolean = false ; 
  compareOutput: boolean = false ; 
  show_download_url: boolean = false;
  apisuccess: boolean = false;
  showsummary: boolean = false ; 
  extracted_with_conflicts: boolean = false;
  manually_resolve_button_click : boolean = false;
  compare_all_button_click : boolean = false;

  constructor(
    private service: ApiService,
    private formBuilder: FormBuilder
    ) {
      this.form = this.formBuilder.group({
        selected_similarities: this.formBuilder.array([], [Validators.required]),
        selected_conflicts: this.formBuilder.array([], [Validators.required]),
        selected_multipleProfiles: this.formBuilder.array([], [Validators.required]),
        selected_singleProfile: this.formBuilder.array([], [Validators.required]),
        selected_cs: this.formBuilder.array([], [Validators.required])
      })
  }

  ngOnInit(): void {
  }

  onCheckboxChange(e) {
    const igComparator: FormArray = this.form.get('igComparator') as FormArray;
    if (e.target.checked) {
      console.log("checked ", e.target.value);
      this.selected_similarities.push(e.target.value);
    }
  }

  onCheckboxChangeCS(e) {
    if (e.target.checked) {
      console.log("checked ", e.target.value);
      this.selected_cs.push(e.target.value);
    }
  }

  reset() {
    window.location.reload();
  }

  clickManuallyResolve() {

    this.getTypesOutput=true;
    this.manually_resolve_button_click = true;

    var ig1 = ((document.getElementById("ig1") as HTMLInputElement).value);
    var ig2 = ((document.getElementById("ig2") as HTMLInputElement).value);
    
    this.service.getTypes(ig1, ig2).subscribe(types => {
      console.log("Response of getTypes API call ", types);
      console.log("Similarities ", types.accumulatedSdTypes.similarities)

      //similarities
      this.similarities = types.accumulatedSdTypes.similarities;

      //UI Conflicts
      this.conflicts = types.accumulatedSdTypes.uiConflicts;
      console.log("Conflicts ", this.conflicts)

      this.split_conflicts(this.conflicts);

      //UI Conflicts for CS types
      this.cs_conflicts = types.accumulatedCsTypes.uiConflicts;
      console.log("CS Conflicts ", this.cs_conflicts)

      //selections
      this.selected_similarities = [];
      this.selected_conflicts = [];
      this.selected_cs = [];
      this.types = types
      this.status = this.types.status;
      this.identifier = this.types.identifier;
    });
  }

  clickCompareAll() {

    this.compareOutput=true;
    this.compare_all_button_click = true;

    var ig1 = ((document.getElementById("ig1") as HTMLInputElement).value);
    console.log(ig1);

    var ig2 = ((document.getElementById("ig2") as HTMLInputElement).value);
    console.log(ig2);
    console.log("clicked compare all button");

    this.service.getTypes(ig1, ig2).subscribe(types => {
      this.types = types
      this.identifier = this.types.identifier;
      this.status = this.types.status;

      console.log("Response: ", this.types);
      console.log("status   ", this.status);
      console.log("identifier   ", this.identifier);

      /*
      if(this.identifier != null && this.status == "success" || this.status == "Extraction successful. Please resolve conflicts to have a more meaningful comparison.") {
        this.apisuccess = true;
      }
      */

      if(this.identifier != null) {
        this.apisuccess = true;
      }
      if(this.identifier != null && this.status == "Extraction successful. Please resolve conflicts to have a more meaningful comparison.") {
        this.extracted_with_conflicts = true;
      }
      /*
      if(this.identifier == null) {
        this.apisuccess = false;
      }*/
    })

    this.service.compare(ig1, ig2).subscribe(objCSStats => {

      console.log("Response: ", objCSStats);
      console.log("identifier   ", this.identifier);
      this.objCSStats = objCSStats;
      console.log("csstats: ", this.objCSStats.capabilityStmtStats.server.fhirVersion["ig1"]);

      this.stats_server_ig1_fhirversion = this.objCSStats.capabilityStmtStats.server.fhirVersion["ig1"];
      this.stats_server_ig1_format = this.objCSStats.capabilityStmtStats.server.format["ig1"];
      this.stats_server_ig1_version = this.objCSStats.capabilityStmtStats.server.version["ig1"];

      this.stats_server_ig2_fhirversion = this.objCSStats.capabilityStmtStats.server.fhirVersion["ig2"];
      this.stats_server_ig2_format = this.objCSStats.capabilityStmtStats.server.format["ig2"];
      this.stats_server_ig2_version = this.objCSStats.capabilityStmtStats.server.version["ig2"];

      this.stats_client_ig1_fhirversion = this.objCSStats.capabilityStmtStats.client.fhirVersion["ig1"];
      this.stats_client_ig1_format = this.objCSStats.capabilityStmtStats.client.format["ig1"];
      this.stats_client_ig1_version = this.objCSStats.capabilityStmtStats.client.version["ig1"];

      this.stats_client_ig2_fhirversion = this.objCSStats.capabilityStmtStats.client.fhirVersion["ig2"];
      this.stats_client_ig2_format = this.objCSStats.capabilityStmtStats.client.format["ig2"];
      this.stats_client_ig2_version = this.objCSStats.capabilityStmtStats.client.version["ig2"];

    });
  }

  clickCompareSelected() {

    this.compareOutput=true;
    this.showsummary=true;

    let sdTypesSim = this.selected_similarities;
    let sdTypesCon = this.selected_conflicts;

    this.sdTypes = sdTypesSim + sdTypesCon;

    if(this.sdTypes.length == 0) {
      this.sdTypes = "None";
    }
    console.log("SD TYPES ", sdTypesSim, sdTypesCon)
    console.log(this.sdTypes);

    this.csTypes = this.selected_cs;
    if(this.csTypes.length == 0) {
      this.csTypes = "None";
    }

    //for updated sd and cs types
    this.service.compareSelected_v1(this.sdTypes, this.csTypes, this.identifier).subscribe(igComparison => {
      console.log("compare selected call ", igComparison);
      this.igComparison = igComparison;
      this.status = this.igComparison.status;
      console.log("STATUS ", this.status );
      this.identifier = this.igComparison.identifier;

      //error handling for "undefined" values, when CS types are not selected by the user
      this.igComparison.capabilityStmtStats = (this.igComparison.capabilityStmtStats === undefined) ? new String("") : this.igComparison.capabilityStmtStats;
      this.igComparison.capabilityStmtStats.server = (this.igComparison.capabilityStmtStats.server === undefined) ? new String("") : this.igComparison.capabilityStmtStats.server;
      this.igComparison.capabilityStmtStats.server.fhirVersion = (this.igComparison.capabilityStmtStats.server.fhirVersion === undefined) ? new String("") : this.igComparison.capabilityStmtStats.server.fhirVersion;
      this.igComparison.capabilityStmtStats.server.format = (this.igComparison.capabilityStmtStats.server.format === undefined) ? new String("") : this.igComparison.capabilityStmtStats.server.format;
      this.igComparison.capabilityStmtStats.server.version = (this.igComparison.capabilityStmtStats.server.version === undefined) ? new String("") : this.igComparison.capabilityStmtStats.server.version;
      this.igComparison.capabilityStmtStats.client = (this.igComparison.capabilityStmtStats.client === undefined) ? new String("") : this.igComparison.capabilityStmtStats.client;
      this.igComparison.capabilityStmtStats.client.fhirVersion = (this.igComparison.capabilityStmtStats.client.fhirVersion === undefined) ? new String("") : this.igComparison.capabilityStmtStats.client.fhirVersion;
      this.igComparison.capabilityStmtStats.client.format = (this.igComparison.capabilityStmtStats.client.format === undefined) ? new String("") : this.igComparison.capabilityStmtStats.client.format;
      this.igComparison.capabilityStmtStats.client.version = (this.igComparison.capabilityStmtStats.client.version === undefined) ? new String("") : this.igComparison.capabilityStmtStats.client.version;

      /*
      if(this.status == "success") {
        this.apisuccess = true;
      }*/

      if(this.identifier != null) {
        this.apisuccess = true;
      }

      //cs stats
      this.stats_server_ig1_fhirversion = this.igComparison.capabilityStmtStats.server.fhirVersion["ig1"];
      this.stats_server_ig1_format = this.igComparison.capabilityStmtStats.server.format["ig1"];
      this.stats_server_ig1_version = this.igComparison.capabilityStmtStats.server.version["ig1"];

      this.stats_server_ig2_fhirversion = this.igComparison.capabilityStmtStats.server.fhirVersion["ig2"];
      this.stats_server_ig2_format = this.igComparison.capabilityStmtStats.server.format["ig2"];
      this.stats_server_ig2_version = this.igComparison.capabilityStmtStats.server.version["ig2"];

      this.stats_client_ig1_fhirversion = this.igComparison.capabilityStmtStats.client.fhirVersion["ig1"];
      this.stats_client_ig1_format = this.igComparison.capabilityStmtStats.client.format["ig1"];
      this.stats_client_ig1_version = this.igComparison.capabilityStmtStats.client.version["ig1"];

      this.stats_client_ig2_fhirversion = this.igComparison.capabilityStmtStats.client.fhirVersion["ig2"];
      this.stats_client_ig2_format = this.igComparison.capabilityStmtStats.client.format["ig2"];
      this.stats_client_ig2_version = this.igComparison.capabilityStmtStats.client.version["ig2"];
    });  
  }

  clickDownloadFile() {
    this.download_url = this.service.downloadFile(this.identifier);
    this.show_download_url=true;
  }

  split_conflicts(conflicts) {

    let tempArray : Array<string>;
    const resourceMap = new Map();

    //create a map of single and multiple profile resources
    for(var i = 0; i < conflicts.length ; i++) {
      tempArray = conflicts[i].split(":")
      if(resourceMap.has(tempArray[0])) {
        if(!resourceMap.get(tempArray[0]).includes(tempArray[1])) {
          resourceMap.set(tempArray[0], "multiple");
        }
      } else {
        resourceMap.set(tempArray[0], tempArray[1]);
      }  
    }
    //split the resources into single and multiple profile arrays
    for(var i = 0; i < conflicts.length ; i++) {
      tempArray = conflicts[i].split(":")
      if(resourceMap.has(tempArray[0])) {

        //get the value of the resource type
        if(resourceMap.get(tempArray[0]) == "multiple") {
          this.multipleProfiles.push(conflicts[i]);
        } else {
          this.singleProfile.push(conflicts[i]);
        }
      }
    }
  }

}
