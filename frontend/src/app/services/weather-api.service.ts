import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WeatherData } from '../models/weather-data';

@Injectable({
  providedIn: 'root'
})
export class WeatherApiService {
  private readonly apiBase = '/api';

  constructor(private readonly http: HttpClient) {}

  getWeather(city: string): Observable<WeatherData> {
    const params = new HttpParams().set('city', city);
    return this.http.get<WeatherData>(`${this.apiBase}/weather`, { params });
  }

  getStatus(): Observable<{ status: string; message: string }> {
    return this.http.get<{ status: string; message: string }>(`${this.apiBase}/status`);
  }
}

