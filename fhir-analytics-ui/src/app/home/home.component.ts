import { HttpResponse } from '@angular/common/http';
import { ThrowStmt } from '@angular/compiler';
import { Component, OnInit, Type } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators, FormArray} from '@angular/forms';
import { ApiService } from '../api.service';
import { ICSStats } from '../ICSStats';
import { IIgArrayList } from '../IIgArrayList';
import { IObjCSStats } from '../IObjCSStats';
import { IStats } from '../IStats';
import { ITypes } from '../ITypes';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  types: ITypes;
  objCSStats : any;
  capabilityStmtStats : ICSStats;
  csStats : ICSStats;
  stats : IStats;

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
  /*
  artifactList: any = [
    { id: 1, name: 'ItSolutionStuff.com' },
    { id: 2, name: 'HDTuto.com' },
    { id: 3, name: 'NiceSnippets.com' }
  ];*/

  ig1List: Array<string>[];
  ig2List: Array<string>[];
  status: string;
  download_url: string;
  identifier: string;
  ig1ArrayList: IIgArrayList;
  accumulatedSdTypes: string[];
  similarities : string[];
  selected_similarities : string[];
  selected_conflicts: string[];
  selected_cs: string[];
  
  getTypesOutput: boolean = false ; 
  compareOutput: boolean = false ; 
  show_download_url: boolean = false;
  apisuccess: boolean = false;

  constructor(
    private service: ApiService,
    private formBuilder: FormBuilder
    ) {
      this.form = this.formBuilder.group({
        selected_similarities: this.formBuilder.array([], [Validators.required])
      })
  }

  ngOnInit(): void {
  }

  onCheckboxChange(e) {
    const igComparator: FormArray = this.form.get('igComparator') as FormArray;
    /* //sample code for checkbox
    if (e.target.checked) {
      console.log("checked ", e.target.value);
      igComparator.push(new FormControl(e.target.value));
    } else {
       const index = igComparator.controls.findIndex(x => x.value === e.target.value);
       igComparator.removeAt(index);
    } */
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
    
  submit(){
    console.log(this.form.value);
  }

  reset() {
    window.location.reload();
  }

  clickGetTypes() {

    this.getTypesOutput=true;

    var ig1 = ((document.getElementById("ig1") as HTMLInputElement).value);
    console.log(ig1);

    var ig2 = ((document.getElementById("ig2") as HTMLInputElement).value);
    console.log(ig2);

    console.log("clicked get types");
    console.log()

    /* ///old api
    this.service.getTypes(ig1, ig2).subscribe(types => {
      this.types = types
      this.status = this.types.status;
      this.identifier = this.types.identifier;
      this.ig1List = this.types.csTypes.ig1;
      this.ig2List = this.types.csTypes.ig2;
      this.accumulatedSdTypes = this.types.accumulatedSdTypes;

      for (let csjson in this.ig1List) {
        console.log(csjson, this.ig1List[csjson]);     
      }
    });*/

    this.service.getTypes(ig1, ig2).subscribe(types => {
      console.log("new get types api ", types);
      console.log("asdsimil ", types.accumulatedSdTypes.similarities)
      this.similarities = types.accumulatedSdTypes.similarities;
      this.selected_similarities = [];
      this.selected_conflicts = [];
      this.selected_cs = [];
      this.types = types
      this.status = this.types.status;
      this.identifier = this.types.identifier;
      this.ig1List = this.types.csTypes.ig1;
      this.ig2List = this.types.csTypes.ig2;
    });
  }

  clickCompareAll() {

    this.compareOutput=true;

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

      if(this.status == "success") {
        this.apisuccess = true;
      }
      
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

  clickCompare() {

    this.compareOutput=true;

    var ig1 = ((document.getElementById("ig1") as HTMLInputElement).value);
    var ig2 = ((document.getElementById("ig2") as HTMLInputElement).value);

    this.service.getIGComparisons();

    this.status = "Success";
    this.download_url = "http://localhost:8081/downloadOnCompare"

    this.service.compare(ig1, ig2).subscribe(objCSStats => {

      console.log("Response: ", objCSStats);
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

  /*
  clickDisplayCSStats() {

    console.log("clicked display cs stats");
    var ig1 = ((document.getElementById("ig1") as HTMLInputElement).value);
    console.log(ig1);

    var ig2 = ((document.getElementById("ig2") as HTMLInputElement).value);
    console.log(ig2);

    this.service.compare(ig1, ig2).subscribe(objCSStats => {

      console.log("Response: ", objCSStats);
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
  */

  clickDownloadFile() {
    
    console.log("Clicked download file button");
    console.log("Identifier: ",this.identifier);
    console.log("identifier   ", this.types.identifier);

    //this.service.downloadFile(this.identifier);
    this.download_url = this.service.downloadFile(this.identifier);

    this.show_download_url=true;
  }

}
