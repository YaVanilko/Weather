import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { WeatherApiService } from './services/weather-api.service';

describe('AppComponent', () => {
  const weatherApiMock: Partial<WeatherApiService> = {
    getStatus: () => of({ status: 'running', message: 'ok' }),
    getWeather: () =>
      of({
        city: 'Dnipro',
        temperature: 20,
        description: 'clear sky',
        icon: '01d',
        humidity: 50,
        windSpeed: 2,
        lastUpdated: Date.now()
      })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{ provide: WeatherApiService, useValue: weatherApiMock }]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should contain predefined cities', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.cities.length).toBeGreaterThan(0);
  });

  it('should render weather header', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Weather Service');
  });
});
