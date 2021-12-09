import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ITypes } from './ITypes';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class ApiService {

  private SERVER_URL = "http://localhost:8081/srvc";

  constructor(private httpClient: HttpClient) { }

  public getTypes(ig1, ig2) : Observable<ITypes>{
    console.log("service call successful - get types ", this.SERVER_URL+`/fhirAnalytics/getTypes?ig1=`+ig1+`&ig2=`+ig2);
    return this.httpClient.get<ITypes>(this.SERVER_URL+`/fhirAnalytics/getTypes?ig1=`+ig1+`&ig2=`+ig2);
  }

  public compare(ig1, ig2) {
    console.log("service call successful - compare ", this.SERVER_URL+`/fhirAnalytics/compare?ig1=`+ig1+`&ig2=`+ig2);
    return this.httpClient.get(this.SERVER_URL+`/fhirAnalytics/compare?ig1=`+ig1+`&ig2=`+ig2);
  }

  public compareSelected(type, sdTypes, identifier) {
    console.log("service call successful - compare selected ",this.SERVER_URL+`/fhirAnalytics/compare?type=`+type+`&sdTypes=`+sdTypes+`&identifier=`+identifier );
    return this.httpClient.get(this.SERVER_URL+`/fhirAnalytics/compare?type=`+type+`&sdTypes=`+sdTypes+`&identifier=`+identifier);
  }

  public compareSelected_v1(sdTypes, csTypes, identifier) {
    console.log("service call successful - compare selected V1 ",this.SERVER_URL+`/fhirAnalytics/compare?sdTypes=`+sdTypes+`&csTypes=`+csTypes+`&identifier=`+identifier );
    return this.httpClient.get(this.SERVER_URL+`/fhirAnalytics/compare?sdTypes=`+sdTypes+`&csTypes=`+csTypes+`&identifier=`+identifier);
  }

  public downloadFile(identifier) {
    console.log("service call successful - downloadFile ", identifier);
    return this.SERVER_URL+`/fhirAnalytics/downloadFile?identifier=`+identifier;
  }
}
