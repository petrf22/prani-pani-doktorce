import { fakeAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { PraniFormComponent } from './prani-form.component';

describe('PraniFormComponent', () => {
  let component: PraniFormComponent;
  let fixture: ComponentFixture<PraniFormComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PraniFormComponent ]
    })
    .compileComponents();
    ;

    fixture = TestBed.createComponent(PraniFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
