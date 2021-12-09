import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { ITypes } from './ITypes';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})

export class ApiService {

  private SERVER_URL = "http://localhost:8081/srvc";

  constructor(private httpClient: HttpClient) { }
  //constructor(private router: Router) {}

  public getURL(){  
		//return this.httpClient.get(this.SERVER_URL);  
    return 1;
	}  

  public getIGComparisons(){  
		console.log("insert the IG comparison here")
    //this.router.navigateByUrl('/comparison-results')
	}  

  public getTypes(ig1, ig2) : Observable<ITypes>{
    console.log("service call successful - get types");
    return this.httpClient.get<ITypes>(this.SERVER_URL+`/fhirAnalytics/getTypes?ig1=`+ig1+`&ig2=`+ig2);
  }

  public compare(ig1, ig2) {
    console.log("service call successful - compare");
    return this.httpClient.get(this.SERVER_URL+`/fhirAnalytics/compare?ig1=`+ig1+`&ig2=`+ig2);
  }

  public downloadFile(identifier) {
    console.log("service call successful - downloadFile ", identifier);
    //return this.httpClient.get(this.SERVER_URL+`/fhirAnalytics/downloadFile?identifier=`+identifier,{responseType: 'blob' as 'json'});
    return this.SERVER_URL+`/fhirAnalytics/downloadFile?identifier=`+identifier;
  }

}
