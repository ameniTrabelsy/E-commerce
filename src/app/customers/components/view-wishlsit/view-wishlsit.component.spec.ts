import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewWishlsitComponent } from './view-wishlsit.component';

describe('ViewWishlsitComponent', () => {
  let component: ViewWishlsitComponent;
  let fixture: ComponentFixture<ViewWishlsitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewWishlsitComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewWishlsitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
