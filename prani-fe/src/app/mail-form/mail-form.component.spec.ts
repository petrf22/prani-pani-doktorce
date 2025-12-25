import { fakeAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { MailFormComponent } from './mail-form.component';

describe('MailFormComponent', () => {
  let component: MailFormComponent;
  let fixture: ComponentFixture<MailFormComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MailFormComponent ]
    })
    .compileComponents();
    ;

    fixture = TestBed.createComponent(MailFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
